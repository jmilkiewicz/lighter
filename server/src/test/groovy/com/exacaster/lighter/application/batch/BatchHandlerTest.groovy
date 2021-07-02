package com.exacaster.lighter.application.batch

import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.backend.Backend
import com.exacaster.lighter.configuration.AppConfiguration
import com.exacaster.lighter.log.LogService
import spock.lang.Specification
import spock.lang.Subject

import static com.exacaster.lighter.test.Factories.applicationInfo
import static com.exacaster.lighter.test.Factories.logs
import static com.exacaster.lighter.test.Factories.newApplication

class BatchHandlerTest extends Specification {

    Backend backend = Mock()

    BatchService service = Mock()

    LogService logService = Mock()

    AppConfiguration config = new AppConfiguration(1, null)

    @Subject
    def handler = Spy(new BatchHandler(backend, service, logService, config))

    def "processing job statuses" () {
        given:
        def app = newApplication()
        def appInfo = applicationInfo(app.id)
        def log = logs(app.id)

        when:
        handler.processRunningBatches()

        then: "updates job status"
        1 * service.fetchRunning() >> [app]
        1 * backend.getInfo(app) >> Optional.of(appInfo)
        1 * service.update({ it.id == app.id && it.state == appInfo.getState()})

        and: "updates job jogs"
        1 * backend.getLogs(app) >> Optional.of(log)
        1 * logService.save(log)
    }

    def "triggering scheduled apps"() {
        given:
        def app = newApplication()

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> []
        1 * service.fetchByState(ApplicationState.NOT_STARTED, _) >> [app]
        1 * handler.launch(app, _) >> {  }
    }

    def "does not trigger when there are no empty slots"() {
        given:
        def app = newApplication()

        when:
        handler.processScheduledBatches()

        then:
        _ * service.fetchRunning() >> [app]
        _ * service.fetchByState(ApplicationState.NOT_STARTED, 0) >> []
        0 * handler.launch(app, _) >> {  }
    }
}
