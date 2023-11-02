package pl.adamsiedlecki.odg.charts


import org.openapitools.model.CreateChartInput
import org.openapitools.model.PresentableOnChart
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import pl.adamsiedlecki.odg.controller.OdgChartsController
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime

@SpringBootTest
class XyChartsApiControllerSpringTest extends Specification {

    def filePath = Path.of("test", "xyChart.png")

    @Autowired
    OdgChartsController odgChartsController

    def "should return xy chart file"() {
        given:
            def input = prepareInput()

        when:
            def result = odgChartsController.createXyChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.OK
            result.body.exists()
            result.body.contentLength() > 1

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, ((ByteArrayResource)result.getBody()).byteArray, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    def "should return xy chart file with red line"() {
        given:
            def input = prepareInput()
            input.setRedValueMarkerLineLevel(BigDecimal.ZERO)
            input.setMaxMinutesConnectingLines(Integer.MAX_VALUE)

        when:
            def result = odgChartsController.createXyChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.OK
            result.body.exists()
            result.body.contentLength() > 1

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, ((ByteArrayResource)result.getBody()).byteArray, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    def "should return xy chart file even though some values are null"() {
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

    def "should return bad request because of too large image size"() {
        given:
            def input = prepareInput()
            input.widthPixels(6550100)

        when:
            def result = odgChartsController.createXyChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.BAD_REQUEST
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
                        .addValueListItem(new PresentableOnChart().time(baseTime.plusHours(3)).value(BigDecimal.valueOf(-2)).groupName("group2"))
    }
}
