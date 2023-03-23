package fr.emevel.jconfig;

import java.io.File;
import java.io.IOException;

public class LocalFileSource extends Source {

    private final SaveMethod saveMethod;
    private final File file;

    private LocalFileSource(boolean saveIfNotExists, boolean threadedAutoSave, SaveMethod saveMethod, File file) {
        super(saveIfNotExists, threadedAutoSave);
        this.saveMethod = saveMethod;
        this.file = file;
    }

    public <T> T load(T data) throws IOException {
        if (!file.exists()) {
            if (saveIfNotExists) {
                save(data);
            }
            return data;
        }
        if (!saveMethod.load(data, file)) {
            if (saveIfNotExists) {
                save(data);
            }
        }
        return data;
    }

    @Override
    public <T> void save(T data) throws IOException {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Cannot create file " + file.getAbsolutePath() + " but it does not exist.");
            }
        }
        saveMethod.save(data, file);
    }

    public static Builder builder(File file) {
        return new Builder(file);
    }

    public static class Builder extends Source.SourceBuilder<Builder> {
        private SaveMethod saveMethod;
        private final File file;

        private Builder(File file) {
            this.file = file;
        }

        public Builder json() {
            return json(0);
        }

        public Builder json(int indentFactor) {
            this.saveMethod = new JsonSaveMethod(indentFactor);
            return this;
        }

        public LocalFileSource build() {
            if (saveMethod == null) {
                throw new IllegalStateException("No save method specified.");
            }
            return new LocalFileSource(saveIfNotExists, threadedAutoSave, saveMethod, file);
        }
    }
}
