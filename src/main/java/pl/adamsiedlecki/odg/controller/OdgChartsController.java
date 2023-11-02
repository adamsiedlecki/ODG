package pl.adamsiedlecki.odg.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.openapitools.api.JfreeChartApi;
import org.openapitools.model.CreateBarChartInput;
import org.openapitools.model.CreateChartInput;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.adamsiedlecki.odg.chart.creator.BarChartCreator;
import pl.adamsiedlecki.odg.chart.creator.XyChartCreator;
import pl.adamsiedlecki.odg.chart.creator.dto.ChartLabels;

@RestController
@RequestMapping("api/v1")
@Slf4j
@RequiredArgsConstructor
public class OdgChartsController implements JfreeChartApi {

    private final XyChartCreator xyChartCreator;
    private final BarChartCreator barChartCreator;

    @Override
    public ResponseEntity<Resource> createXyChart(CreateChartInput input) {
        log.info("Received CreateChartInput: {}", input);

        if (isSizeTooBig(input.getWidthPixels(), input.getHeightPixels())) {
            log.error("Image size is too big");
            return ResponseEntity.badRequest().build();
        }
        if (input.getValueList().isEmpty()) {
            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).build();
        }

        ChartLabels chartLabels = new ChartLabels(input.getChartTitle(), input.getValuesLabel(), input.getTimeLabel());
        try {
            var byteArrayResource = xyChartCreator.createChart(input.getValueList(),
                    input.getWidthPixels(),
                    input.getHeightPixels(),
                    chartLabels,
                    input.getAreItemLabelsVisible(),
                    input.getMaxMinutesConnectingLines(),
                    input.getRedValueMarkerLineLevel());
            if(byteArrayResource == null) {
                return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
            }
            return ResponseEntity.ok(byteArrayResource);
        } catch (Exception e) {
            log.error("Controller error: ", e);
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Resource> createBarChart(CreateBarChartInput barInput) {
        if (isSizeTooBig(barInput.getWidthPixels(), barInput.getHeightPixels())) {
            log.error("Image size is too big");
            return ResponseEntity.badRequest().build();
        }
        log.info("Creating bar chart: {}", barInput);
        var byteArrayResource = barChartCreator.createChart(barInput.getValueList(),
                barInput.getWidthPixels(),
                barInput.getHeightPixels(),
                barInput.getChartTitle(),
                barInput.getCategoriesLabel(),
                barInput.getValuesLabel());
        return ResponseEntity.ok(byteArrayResource);
    }

    private boolean isSizeTooBig(int width, int height) {
        return width > 50000 || height > 50000;
    }
}
