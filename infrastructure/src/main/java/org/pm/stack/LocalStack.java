package org.pm.stack;

//import com.amazonaws.services.ec2.model.Vpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.constructs.Construct;

public class LocalStack extends Stack {
    private final Vpc vpc;
    public LocalStack(final App scope, final String id, final StackProps props) {

        super(scope, id, props);
        this.vpc=createVpc();
        DatabaseInstance authServiceDb= creatDatabase("AuthServiceDB","auth-service-db");

        DatabaseInstance patientServiceDb= creatDatabase("PatientServiceDB","patient-service-db");


        CfnHealthCheck authServiceDbHealthCheck=createDbHealthCheck(authServiceDb,"AuthServiceDBHealthCheck");
        CfnHealthCheck patientServiceDbHealthCheck=createDbHealthCheck(patientServiceDb,"PatientServiceDBHealthCheck");
    }
    private Vpc createVpc() {
        return Vpc.Builder.create(this,"PatientManagementVPC").vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance creatDatabase(String id, String dbName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()))
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder.create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                .type("TCP")
                .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                .build())
                .build();
    }

    public static void main(final String[] args) {
        App app = new App(AppProps.builder().outdir("./infrastructure/cdk.out").build());
        StackProps props = StackProps.builder().
                synthesizer(new BootstraplessSynthesizer()).build();

        new LocalStack(app, "LocalStack", props);
        app.synth();
        System.out.println("App synthesizing in progress... ");
    }
}
