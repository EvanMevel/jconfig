package fr.emevel.jconfig;

import java.io.IOException;

public abstract class Source {

    protected final boolean saveIfNotExists;
    protected final boolean threadedAutoSave;

    protected Source(boolean saveIfNotExists, boolean threadedAutoSave) {
        this.saveIfNotExists = saveIfNotExists;
        this.threadedAutoSave = threadedAutoSave;
    }

    protected abstract <T> T load(T data) throws IOException;

    public <T> T load(Class<T> type) throws IOException {
        T data = ReflectionUtils.defaultInstance(type);
        return load(data);
    }

    public <T> T loadAutoSave(Class<T> type) throws IOException {
        T data = AutoSaver.createAutoSaveWrapper(type, this, threadedAutoSave);
        return load(data);
    }

    public abstract <T> void save(T data) throws IOException;

    @SuppressWarnings("unchecked")
    public abstract static class SourceBuilder<T extends SourceBuilder<T>> {
        protected boolean saveIfNotExists = true;
        protected boolean threadedAutoSave = false;

        public T saveIfNotExists(boolean saveIfNotExists) {
            this.saveIfNotExists = saveIfNotExists;
            return (T) this;
        }

        public T threadedAutoSave(boolean threadedAutoSave) {
            this.threadedAutoSave = threadedAutoSave;
            return (T) this;
        }

        public abstract Source build();
    }

}
