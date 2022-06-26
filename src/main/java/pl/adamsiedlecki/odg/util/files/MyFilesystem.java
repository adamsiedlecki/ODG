package pl.adamsiedlecki.odg.util.files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MyFilesystem {

    private static final long TWO_WEEKS_MILLIS = 1000L * 60 * 60 * 24 * 14;
    private static final int MILLIS_IN_A_SECOND = 1000;
    private static final String SEP = File.separator;
    private static final String STORAGE = "storage";
    private static final String THIS_IS_NOT_A_DIRECTORY = "This is not a directory: {}";

    public static String getStoragePath() {
        if (SystemDetect.isUnix()) {
            return SEP + STORAGE + SEP;
        } else {
            return STORAGE + SEP;
        }
    }

    public String getChartsPath() {
        String path = STORAGE + SEP + "img" + SEP + "charts" + SEP;
        if (SystemDetect.isUnix()) {
            return SEP + path;
        } else {
            return path;
        }
    }

    public void removeAllFilesFromFileSystem() {
        deleteAllFilesInDirectory(new File(getStoragePath()), 0);
    }

    public boolean fileExistsAndIsNoOlderThanXSeconds(File file, long x) {
        return file.exists() && (System.currentTimeMillis() - file.lastModified()) < (x * MILLIS_IN_A_SECOND);
    }

    @Scheduled(cron = "0 0 0 1 * *") // first day of month
    public void cleanupOldFiles() {
        log.info("Performing old files cleanup");
        deleteAllFilesInDirectory(new File(getChartsPath()), TWO_WEEKS_MILLIS);
    }

    @PostConstruct
    public void makeAllDirectories() {
        if (!new File(getChartsPath()).mkdirs()) {
            log.warn("Cannot create directories for charts");
        }
    }

    private void deleteAllFilesInDirectory(File file, long minimalAgeMillis) {
        if (!file.isDirectory()) {
            deleteFile(file);
            return;
        }
        final File[] filesToDelete = file.listFiles(f -> {
            BasicFileAttributes attributes;
            try {
                attributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                return System.currentTimeMillis() - attributes.lastModifiedTime().toMillis() >= minimalAgeMillis;
            } catch (IOException e) {
                log.error("Failed to read file attributes: {}", e.getMessage());
            }
            return true;
        });
        if (filesToDelete == null) {
            log.error("FilesToDelete array is null!");
            return;
        }
        for (File fileToDelete : filesToDelete) {
            deleteAllFilesInDirectory(fileToDelete, minimalAgeMillis);
        }
    }

    private void deleteFile(File fileToDelete) {
        try {
            Files.delete(fileToDelete.toPath());
            log.info("Deleted file successfully: {}", fileToDelete.getName());
        } catch (IOException e) {
            log.error("There was an error deleting a file: {}", e.getMessage());
        }
    }

    public File[] getAllFilesInDirectory(File directory) {
        if (!directory.isDirectory()) {
            log.error(THIS_IS_NOT_A_DIRECTORY, directory.getAbsolutePath());
            return new File[]{};
        }
        return directory.listFiles();
    }

    public Optional<BasicFileAttributes> getFileAttributes(File file) {
        try {
            return Optional.of(Files.readAttributes(file.toPath(), BasicFileAttributes.class));
        } catch (IOException e) {
            log.error("Failed to read file attributes: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public List<File> getAllFilesRecursively(String directory) {
        if (!new File(directory).isDirectory()) {
            log.error(THIS_IS_NOT_A_DIRECTORY, directory);
            return List.of();
        }
        Path path = Paths.get(directory);
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to get all files recursively {}", e.getMessage());
        }
        return List.of();
    }
}
