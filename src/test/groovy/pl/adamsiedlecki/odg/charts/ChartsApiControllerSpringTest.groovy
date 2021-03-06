package pl.adamsiedlecki.odg.charts


import org.openapitools.model.CreateChartInput
import org.openapitools.model.PresentableOnChart
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import pl.adamsiedlecki.odg.controller.OdgChartsController
import spock.lang.Specification

import java.time.LocalDateTime

@SpringBootTest
class ChartsApiControllerSpringTest extends Specification {

    @Autowired
    OdgChartsController odgChartsController

    def "should return chart file"() {
        given:
            def input = prepareInput()

        when:
            def result = odgChartsController.createXyChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.OK
            result.body.exists()
            result.body.contentLength() > 1
    }

    def "should return chart file even though some values are null"() {
        given:
            def input = prepareInput()
                    .chartTitle(null)
                    .valuesLabel(null)
                    .timeLabel(null)
                    .widthPixels(700)
                    .heightPixels(500)
                    .areItemLabelsVisible(true)
                    .maxMinutesConnectingLines(2)

        when:
            def result = odgChartsController.createXyChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.OK
            result.body.exists()
            result.body.contentLength() > 1
    }

    def "should return bad request because of no presentable on chart data"() {
        given:
            def input = prepareInput()
            input.valueList(List.of())

        when:
            def result = odgChartsController.createXyChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.BAD_REQUEST
            result.body == null
    }

    def "should return internal error because of too large image size"() {
        given:
            def input = prepareInput()
            input.widthPixels(65501)

        when:
            def result = odgChartsController.createXyChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
            result.body == null
    }

    CreateChartInput prepareInput() {
        def baseTime = LocalDateTime.now()

        return new CreateChartInput()
                        .chartTitle("example chart title")
                        .valuesLabel("example values label")
                        .timeLabel("example time label")
                        .widthPixels(700)
                        .heightPixels(500)
                        .areItemLabelsVisible(false)
                        .maxMinutesConnectingLines(2)

                        .addValueListItem(new PresentableOnChart().time(baseTime).value(BigDecimal.valueOf(2)).groupName("group1"))
                        .addValueListItem(new PresentableOnChart().time(baseTime.plusHours(1)).value(BigDecimal.valueOf(3)).groupName("group1"))
                        .addValueListItem(new PresentableOnChart().time(baseTime.plusHours(4)).value(BigDecimal.valueOf(5)).groupName("group1"))

                        .addValueListItem(new PresentableOnChart().time(baseTime).value(BigDecimal.valueOf(4)).groupName("group2"))
                        .addValueListItem(new PresentableOnChart().time(baseTime.plusHours(1)).value(BigDecimal.valueOf(7)).groupName("group2"))
                        .addValueListItem(new PresentableOnChart().time(baseTime.plusHours(3)).value(BigDecimal.valueOf(2)).groupName("group2"))
    }
}
