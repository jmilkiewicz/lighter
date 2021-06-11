package com.exacaster.lighter.application.batch;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.Storage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class BatchService {

    private final Storage storage;
    private final Backend backend;

    public BatchService(Storage storage, Backend backend) {
        this.storage = storage;
        this.backend = backend;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return storage.findApplications(ApplicationType.BATCH, from, size);
    }

    public Application create(SubmitParams batch) {
        var entity = ApplicationBuilder.builder()
                .setId(UUID.randomUUID().toString())
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(batch)
                .setCreatedAt(LocalDateTime.now())
                .build();
        return storage.saveApplication(entity);
    }

    public Application update(Application application) {
        return storage.saveApplication(application);
    }

    public List<Application> fetchByState(ApplicationState state, Integer limit) {
        return storage.findApplicationsByStates(ApplicationType.BATCH, List.of(state), limit);
    }

    public List<Application> fetchNonFinished() {
        return storage.findApplicationsByStates(ApplicationType.BATCH, ApplicationState.incompleteStates(), Integer.MAX_VALUE);
    }

    public Optional<Application> fetchOne(String id) {
        return storage.findApplication(id);
    }

    public void deleteOne(String id) {
        backend.kill(id);
        storage.deleteApplication(id);
    }

}
