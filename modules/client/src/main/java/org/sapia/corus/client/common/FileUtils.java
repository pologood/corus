package org.sapia.corus.client.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Hold various file-related utility methods.
 * 
 * @author yduchesne
 * 
 */
public class FileUtils {

  public static class FileInfo {
    public String directory;
    public String fileName;
    public boolean isClasses;
  }

  /**
   * @param dir
   *          a {@link String} corresponding to the path to a file or directory.
   * @return
   */
  public static boolean isAbsolute(String path) {
    return path.startsWith("/") || isWindowsDrive(path);
  }

  /**
   * @param c
   *          a <code>char</code> to test.
   * @return <code>true</code> if the given character corresponds to a path
   *         separator.
   */
  public static boolean isPathSeparator(char c) {
    return c == '/' || c == '\\';
  }

  /**
   * @param path
   *          a {@link String} corresponding to the path to a file or directory.
   * @return <code>true</code> if the path starts with a Windows drive
   *         identifier (e.g.: <code>C:</code>).
   */
  public static boolean isWindowsDrive(String path) {
    return path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':';
  }

  /**
   * @param directory
   *          the path for which to obtain the corresponding {@link FileInfo}.
   * @return a {@link FileInfo}.
   */
  public static FileInfo getFileInfo(String path) {
    String trimmedPath = path.trim();
    int separatorIndex = trimmedPath.lastIndexOf("/");
    FileInfo info = new FileInfo();
    if (trimmedPath.endsWith("/")) {
      info.directory = path;
      info.isClasses = true;
    } else if (separatorIndex > 1) {
      String fileName = path.substring(separatorIndex + 1);
      if (fileName.contains(".")) {
        info.directory = path.substring(0, separatorIndex);
        info.fileName = fileName;
      } else {
        info.directory = path;
      }
    } else {
      info.directory = path;
    }
    return info;
  }
  
  /**
   * Splits a string representing a list of file paths.
   * 
   * @param paths a list of file paths, delimited by <code>;</code> or <code>:</code>.
   * @return the array of file paths.
   */
  public static String[] splitFilePaths(String paths) {
    List<String> toReturn = new ArrayList<>();
    StringTokenizer tk = new StringTokenizer(paths, ":;");
    while (tk.hasMoreTokens()) {
      toReturn.add(tk.nextToken());
    }
    return toReturn.toArray(new String[toReturn.size()]);
  }
  
  /**
   * Merges the given paths into a single string, wherein paths are separated
   * by a path separator (either <code>;</code> or <code>:</code>, depending on 
   * the OS).
   * 
   * @param paths some paths to merge.
   * @return the given paths, merged together in a single path string.
   * @see File#pathSeparator
   */
  public static String mergeFilePaths(String...paths) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < paths.length; i++) {
      if (i > 0) {
        sb.append(File.pathSeparatorChar);
      }
      sb.append(paths[i]);
    }
    return sb.toString();
  }
  
  /**
   * Appends <code>subPath</code> to <code>path</code>, adding a "/" character
   * in between as a separator, if required.
   * 
   * @param path
   *          the path to which to append a sub-path.
   * @param subPath
   *          the sub-path to append to the given path.
   * @return the path resulting from the concatenation.
   */
  public static final String append(String path, String subPath) {
    StringBuilder sb = new StringBuilder(path);
    append(sb, subPath);
    return sb.toString();
  }

  /**
   * Appends <code>subPath</code> to <code>path</code>, adding a file separator character
   * in between, if required.
   * 
   * @param path
   *          the path to which to append a sub-path.
   * @param subPath
   *          the sub-path to append to the given path.
   */
  public static final void append(StringBuilder path, String subPath) {
    if (subPath == null || subPath.length() == 0) {
      return;
    }
    if (subPath.startsWith("/") || subPath.startsWith("\\")) {
      if (subPath.length() == 1) {
        return;
      }
      subPath = subPath.substring(1);
    }
    if (path.length() > 0 && (path.charAt(path.length() - 1) == '/' || path.charAt(path.length() - 1) == '\\')) {
      path.append(subPath);
    } else {
      path.append(File.separator).append(subPath);
    }
  }

  /**
   * Concatenates the given paths, putting a file separator in between each.
   * 
   * @param toConcat
   *          the paths to concatenate.
   * @return the concatenated path, with a separator in between.
   */
  public static final String toPath(String... toConcat) {
    StringBuilder sb = new StringBuilder();
    if (toConcat != null) {
      for (int i = 0; i < toConcat.length; i++) {
        if (i > 0) {
          sb.append("/");
        }
        sb.append(toConcat[i]);
      }
    }
    return sb.toString();
  }
}
