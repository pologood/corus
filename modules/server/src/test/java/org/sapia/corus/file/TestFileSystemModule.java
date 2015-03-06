package org.sapia.corus.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.services.file.FileSystemModule;

public class TestFileSystemModule implements FileSystemModule{
  
  @Override
  public void createDirectory(File dir) throws IOException {
  }
  
  @Override
  public void deleteDirectory(File dir) throws IOException {
  }
  
  @Override
  public void deleteFile(File file) throws IOException {
  }
  
  @Override
  public boolean exists(File toCheck) {
    return true;
  }

  @Override
  public InputStream openZipEntryStream(File zipFile, String entryName)
      throws IOException {
    return null;
  }
  
  @Override
  public void unzip(File doUnzip, File destDir) throws IOException {
  }
  
  @Override
  public void zip(File destFile, File srcDir) throws IOException {
  }
  
  @Override
  public List<File> listFiles(File baseDir) {
    return new ArrayList<File>();
  }
  
  @Override
  public File getFileHandle(String path) {
    return new File(path);
  }
  
  @Override
  public Reader getFileReader(File f) throws FileNotFoundException, IOException {
    return new FileReader(f);
  }
  
  @Override
  public void renameDirectory(File from, File to) throws IOException {
  }
  
}
