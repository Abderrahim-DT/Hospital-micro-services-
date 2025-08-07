#!/usr/bin/env bash

set -e

# Configuration des credentials pour LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

echo "🔍 Vérification de LocalStack..."
if ! curl -s http://localhost:4566/_localstack/health > /dev/null; then
    echo "❌ LocalStack n'est pas accessible sur localhost:4566"
    exit 1
fi

echo "✅ LocalStack est accessible"

# Nettoyage de la stack existante
echo "🧹 Nettoyage des stacks existantes..."
aws --endpoint-url=http://localhost:4566 cloudformation delete-stack \
    --stack-name patient-management 2>/dev/null || true

echo "⏳ Attente de la suppression complète..."
aws --endpoint-url=http://localhost:4566 cloudformation wait stack-delete-complete \
    --stack-name patient-management 2>/dev/null || true

echo "🚀 Déploiement de la nouvelle stack..."

# Déploiement avec timeout étendu pour LocalStack Pro
timeout 900 aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name patient-management \
    --template-file "./cdk.out/LocalStack.template.json" \
    --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
    --no-fail-on-empty-changeset

if [ $? -eq 0 ]; then
    echo "✅ Déploiement réussi!"
    
    # Vérifiez les ressources créées
    echo "📋 Ressources créées:"
    aws --endpoint-url=http://localhost:4566 cloudformation list-stack-resources \
        --stack-name patient-management \
        --query 'StackResourceSummaries[*].[ResourceType,LogicalResourceId,ResourceStatus]' \
        --output table
    
    # Vérifiez les load balancers
    echo "🔍 Recherche des load balancers..."
    aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
        --query "LoadBalancers[0].DNSName" --output text 2>/dev/null || echo "Aucun load balancer trouvé"
        
    # Vérifiez les clusters ECS
    echo "📦 Clusters ECS:"
    aws --endpoint-url=http://localhost:4566 ecs list-clusters \
        --query 'clusterArns' --output table 2>/dev/null || echo "Aucun cluster ECS trouvé"
        
    # Vérifiez les bases de données RDS
    echo "🗄️ Instances RDS:"
    aws --endpoint-url=http://localhost:4566 rds describe-db-instances \
        --query 'DBInstances[*].[DBInstanceIdentifier,DBInstanceStatus,Engine]' \
        --output table 2>/dev/null || echo "Aucune instance RDS trouvée"
        
else
    echo "❌ Échec du déploiement"
    
    # Affichez les événements en cas d'erreur
    echo "📋 Derniers événements de la stack:"
    aws --endpoint-url=http://localhost:4566 cloudformation describe-stack-events \
        --stack-name patient-management \
        --query 'StackEvents[0:10].[Timestamp,ResourceStatus,ResourceType,LogicalResourceId,ResourceStatusReason]' \
        --output table 2>/dev/null || echo "Impossible de récupérer les événements"
fi