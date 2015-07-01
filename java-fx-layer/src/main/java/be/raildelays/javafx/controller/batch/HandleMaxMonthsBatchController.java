package be.raildelays.javafx.controller.batch;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * @author Almex
 * @since 1.2
 */
public class HandleMaxMonthsBatchController extends AbstractBatchController {
    @FXML
    private DatePicker date;

    @Override
    public void doStart() {
        if (date.getValue() != null) {
            JobParameters jobParameters = propertiesExtractor.getJobParameters(null, null);
            JobParametersBuilder builder = new JobParametersBuilder(jobParameters);

            startButton.setDisable(true);
            stopButton.setDisable(false);
            abandonButton.setDisable(true);
            restartButton.setDisable(true);
            progressBar.setProgress(0.0);
            progressIndicator.setProgress(0.0);
            progressLabel.setText("");

            if (service.isRunning()) {
                service.cancel();
            }


            builder.addDate("threshold.date", Date.from(date.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

            service.reset();
            service.start(jobName, builder.toJobParameters());

            doRefreshProgress();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        setJobName("handleMaxMonthsJob");
        date.setValue(LocalDate.now());
        date.setDayCellFactory(param -> new DateCell() {

            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(LocalDate.now().minusDays(6)) ||
                        item.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });
        super.initialize(location, resources);
    }

    @Override
    protected ChangeListener<Worker.State> getStateChangeListener() {

        return (ObservableValue<? extends Worker.State> observable,
                Worker.State oldValue,
                Worker.State newValue) -> {
            super.getStateChangeListener().changed(observable, oldValue, newValue);
            date.setDisable(startButton.isDisable());
        };

    }

    @Override
    protected void resetButtons() {
        super.resetButtons();
        date.setDisable(startButton.isDisable());
    }
}