package com.khaleo.flashcard.contract.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DeployWorkflowContractTest {

    @Test
    void shouldEncodeMainPushTriggerAndFailureOnAnyTarget() throws Exception {
        String workflow = Files.readString(Path.of("../.github/workflows/deploy-backend.yml"));

        assertThat(workflow).contains("workflow_call:");
        assertThat(workflow).contains("TARGET_ENV");
        assertThat(workflow).contains("docker/build-push-action");
        assertThat(workflow).contains("aws ssm send-command");
        assertThat(workflow).contains("Deploy backend container to EC2 via SSM");
        assertThat(workflow).contains("exit 1");
    }
}
