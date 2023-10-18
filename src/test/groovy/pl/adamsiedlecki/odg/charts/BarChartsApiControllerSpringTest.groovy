package pl.adamsiedlecki.odg.charts

import org.jfree.chart.ChartUtils
import org.openapitools.model.CreateBarChartInput
import org.openapitools.model.PresentableOnBarChart
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import pl.adamsiedlecki.odg.controller.OdgChartsController
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@SpringBootTest
class BarChartsApiControllerSpringTest extends Specification {

    def filePath = Path.of("test", "barChart.png")

    @Autowired
    OdgChartsController odgChartsController

    def "should return bar chart file with subcategories"() {
        given:
            def input = prepareSubCategoriesInput()

        when:
            def result = odgChartsController.createBarChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.OK
            result.body.exists()
            result.body.contentLength() > 1

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, ((ByteArrayResource)result.getBody()).byteArray, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    def "should return simple bar chart file"() {
        given:
            def input = prepareSimpleInput()

        when:
            def result = odgChartsController.createBarChart(input)

        then:
            result != null
            result.statusCode == HttpStatus.OK
            result.body.exists()
            result.body.contentLength() > 1

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, ((ByteArrayResource)result.getBody()).byteArray, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private CreateBarChartInput prepareSubCategoriesInput() {
        return new CreateBarChartInput()
                        .chartTitle("example chart title")
                        .valuesLabel("example values label")
                        .categoriesLabel("categotries label")
                        .widthPixels(700)
                        .heightPixels(500)
                        .areItemLabelsVisible(false)

                        .addValueListItem(new PresentableOnBarChart().categoryName("kat1").value(bd(10)).subCategoryName("temperature"))
                        .addValueListItem(new PresentableOnBarChart().categoryName("kat1").value(bd(20)).subCategoryName("humidity"))

                        .addValueListItem(new PresentableOnBarChart().categoryName("kat2").value(bd(11)).subCategoryName("temperature"))
                        .addValueListItem(new PresentableOnBarChart().categoryName("kat2").value(bd(22)).subCategoryName("humidity"))

                        .addValueListItem(new PresentableOnBarChart().categoryName("kat3").value(bd(2)).subCategoryName("fajność"))
                        .addValueListItem(new PresentableOnBarChart().categoryName("kat3").value(bd(0)).subCategoryName("humidity"))

    }

    private CreateBarChartInput prepareSimpleInput() {
        return new CreateBarChartInput()
                .chartTitle("example chart title")
                .valuesLabel("example values label")
                .categoriesLabel("categotries label")
                .widthPixels(700)
                .heightPixels(500)
                .areItemLabelsVisible(false)

                .addValueListItem(new PresentableOnBarChart().categoryName("stacja1").value(bd(10)).subCategoryName(""))
                .addValueListItem(new PresentableOnBarChart().categoryName("stacja2").value(bd(11)).subCategoryName(""))
                .addValueListItem(new PresentableOnBarChart().categoryName("stacja3").value(bd(2)).subCategoryName(""))

    }

    private BigDecimal bd(int n) {
        return BigDecimal.valueOf(n);
    }
}
