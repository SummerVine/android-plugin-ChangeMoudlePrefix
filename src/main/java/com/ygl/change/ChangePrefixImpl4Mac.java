package com.ygl.change;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jasoncool jasoncool_521@qq.com
 * @Description:android module资源重命名的工具类
 */
class ChangePrefixImpl4Mac {

    private static final String DOT = ".";
    private static final String SPLIT_STRING = "_";
    private static final String JAVA_FILE = ".java";
    private static final String XML_FILE = ".xml";
    private static final String FILE_ACCESS = "rw";

    static void startRenameTask(String renameModulePath, String modulePrefix) {
        File fileDir = new File(renameModulePath);
        if (fileDir != null) {
            String renameResPath = renameModulePath + "/src/main/res/";
            File renameFileDir = new File(renameResPath);
            String workSpaceDir = renameModulePath + "/src/main/";
            File[] readyFiles = renameFileDir.listFiles();
            renameResFiles(readyFiles, modulePrefix, workSpaceDir, SPLIT_STRING);

        }
    }

    private static boolean isEndOfFile(File file) {
        try (RandomAccessFile randomFile = new RandomAccessFile(file, FILE_ACCESS)) {
            randomFile.seek(file.length() - 1);
            int target = randomFile.read();
            Boolean hasReturn = 10 == target;
            randomFile.close();
            return hasReturn;
        } catch (Exception ignored) {
        }

        return false;
    }


    private static void renameSingleFile(File fileItem, String modulePrefix, String workSpacePath, String splitString) {
        System.out.println("rename file：" + fileItem.getParent() + "/" + fileItem.getName());
        String path = fileItem.getParent();
        String fileName = fileItem.getName();
        if (!fileName.startsWith(modulePrefix) && !path.contains("values")) {
            String newFileName = modulePrefix + splitString + fileName;
            if (fileItem.renameTo(new File(path + "/" + newFileName))) {
                System.out.println("rename file " + newFileName + " success!");
                scanLinks(path, fileName.substring(0, fileName.indexOf(".")), newFileName.substring(0, newFileName.indexOf(".")), workSpacePath);
            } else {
                System.out.println(path + fileName + "edit failed");
            }
        }


    }

    private static void scanLinks(String path, String fileName, String newFileName, String workSpacePath) {
        File workSpaceDir = new File(workSpacePath);
        File[] workFiles = workSpaceDir.listFiles();
        if (workFiles != null) {
            for (File fileItem :
                    workFiles) {
                if (fileItem != null && fileItem.isDirectory()) {
                    scanLinks(path, fileName, newFileName, fileItem.getPath());
                } else {
                    assert fileItem != null;
                    doCheckFile(path, fileName, newFileName, fileItem);
                }
            }
        }
    }


    private static boolean filterReplaceResFile(String fileItem) {
        return fileItem != null && (!fileItem.startsWith(DOT));
    }


    private static void doCheckFile(String path, String fileName, String newFileName, File fileItem) {
        if (findTargetFileType(fileItem.getName())) {
            updateFileContent(path, fileItem, fileName, newFileName, fileItem.getName().endsWith(JAVA_FILE));
        }

    }

    private static void updateFileContent(String path, File file, String oldstr, String newStr, Boolean isJavaFile) {

        boolean hasReturn = isEndOfFile(file);
        FileReader in;
        try {
            in = new FileReader(file);

            BufferedReader bufIn = new BufferedReader(in);

            CharArrayWriter tempStream = new CharArrayWriter();

            String line;
            String nextLine;
            line = bufIn.readLine();
            while (line != null) {
                line = replaceTargetString(isJavaFile, oldstr, newStr, line, getPathSuffix(path));
                tempStream.write(line);
                nextLine = bufIn.readLine();
                if (nextLine == null) {
                    if (hasReturn) {
                        tempStream.append(System.getProperty("line.separator"));
                    }
                    break;
                } else {
                    tempStream.append(System.getProperty("line.separator"));
                    line = nextLine;
                }

            }

            bufIn.close();
            FileWriter out = new FileWriter(file);
            tempStream.writeTo(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPathSuffix(String path) {

        String sufix = path.substring(path.lastIndexOf("/") + 1, path.length());

        if (sufix.contains("-")) {
            return sufix.substring(0, sufix.indexOf("-"));
        }
        return sufix;

    }


    private static boolean findTargetFileType(String name) {
        return name != null && (name.endsWith(JAVA_FILE) || name.endsWith(XML_FILE));
    }


    private static String replaceTargetString(Boolean isJavaFile, String oldstr, String newStr, String line, String endParentPath) {
        if (endParentPath.equals("values")) {
            return line;
        }
        String changeLine = ";" + line + ";";
        String noNumNolatterReg = "([^_|a-z|A-Z|0-9])";
        if (isJavaFile) {
            String reg = noNumNolatterReg + "(R." + endParentPath + "." + oldstr + ")" + noNumNolatterReg;
            Pattern p = Pattern.compile(reg);

            Matcher matcher = p.matcher(changeLine);
            if (matcher.find()) {
                line = changeLine.replaceAll(reg, "$1R." + endParentPath + "." + newStr + "$3");
                line = line.replaceFirst(";", "");
                line = line.substring(0, line.length() - 1);
            }

        } else {
            String reg = noNumNolatterReg + "(@" + endParentPath + "/" + oldstr + ")" + noNumNolatterReg;
            Pattern p = Pattern.compile(reg);
            Matcher matcher = p.matcher(changeLine);
            if (matcher.find()) {
                line = changeLine.replaceAll(reg, "$1@" + endParentPath + "/" + newStr + "$3");
                line = line.replaceFirst(";", "");
                line = line.substring(0, line.length() - 1);
            }
        }
        return line;
    }

    private static void renameResFiles(File[] files, String modulePrefix, String workSpacePath, String splitString) {
        if (files == null) {
            System.out.println("重命名的文件夹下没有任何文件");
            return;
        }
        for (File fileItem : files
        ) {
            if (fileItem != null && fileItem.isDirectory() && filterReplaceResFile(fileItem.getName())) {
                File[] readyFiles = fileItem.listFiles();
                renameResFiles(readyFiles, modulePrefix, workSpacePath, splitString);
            } else {
                assert fileItem != null;
                renameSingleFile(fileItem, modulePrefix, workSpacePath, splitString);

            }

        }
    }
}
