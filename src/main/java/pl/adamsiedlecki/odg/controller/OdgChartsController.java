package pl.adamsiedlecki.odg.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.openapitools.api.JfreeChartApi;
import org.openapitools.model.CreateChartInput;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.adamsiedlecki.odg.chart.creator.SimpleChartCreator;
import pl.adamsiedlecki.odg.chart.creator.dto.ChartLabels;

import java.io.File;

@RestController
@RequestMapping("api/v1")
@Slf4j
@RequiredArgsConstructor
public class OdgChartsController implements JfreeChartApi {

    private final  SimpleChartCreator simpleChartCreator;

    @Override
    public ResponseEntity<Resource> createXyChart(CreateChartInput input) {
        log.info("Received CreateChartInput: {}", input);

        if (input.getValueList().isEmpty()) {
            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).build();
        }

        ChartLabels chartLabels = new ChartLabels(input.getChartTitle(), input.getValuesLabel(), input.getTimeLabel());
        try {
            File chart = simpleChartCreator.createChart(input.getValueList(), input.getWidthPixels(), input.getHeightPixels(), chartLabels, input.getAreItemLabelsVisible(), input.getMaxMinutesConnectingLines());
            if(chart == null || !chart.exists()) {
                return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
            }
            return ResponseEntity.ok(new FileSystemResource(chart));
        } catch (Exception e) {
            log.error("Controller error: ", e);
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }
}
