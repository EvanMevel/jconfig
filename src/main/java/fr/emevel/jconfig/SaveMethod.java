package fr.emevel.jconfig;

import java.io.File;
import java.io.IOException;

public interface SaveMethod {

    <T> boolean load(T data, File file) throws IOException;

    <T> void save(T data, File file) throws IOException;

}
