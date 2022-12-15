package com.example.android_nas_sync.common;
import com.example.android_nas_sync.models.Mapping;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class FileScanner {
//    static String ipAddress = "192.168.0.45";
//    static String username = "file-sync";
//    static String password = "file-sync";
//    static String shareName = "backup";
//    static String shareFolder = "phone_pixel/camera/";
//    static String phoneFolder = "src/img";

    public void refreshAllMappings(List<Mapping> mappings){

    }

    // TODO async
    public static ScanResult refreshMapping(Mapping mapping){
        SMBClient client = new SMBClient();
        Connection connection = null;
        try{
             connection = client.connect(mapping.getServerIp());
        }
        catch(Exception e){
            client.close();
            return new ScanResult(false, "Unable to find server");
        }

        Session session = null;
        try{
            AuthenticationContext authContext =
                    new AuthenticationContext(mapping.getUsername(), mapping.getPassword().toCharArray(), mapping.getServerIp());
            session = connection.authenticate(authContext);
        }
        catch(Exception e){
            client.close();
            return new ScanResult(false, "Unable to authenticate to server");
        }

        DiskShare share = null;
        try{
            share = (DiskShare) session.connectShare(mapping.getDestinationShare());
        }
        catch (Exception e){
            client.close();
            return new ScanResult(false, "Unable to connect to share");
        }

        List<File> newFiles = null;
        try{
            newFiles = scanNewFiles(share, mapping.getDestinationShare(), mapping.getSourceFolder());
        }
        catch(Exception e){
            client.close();
            return new ScanResult(false, "Failed to scan phone files");
        }

        try {
            // TODO make non atomic
            for (File file : newFiles) {
                writeToFile(share, file, mapping.getDestinationShare());
            }
        }
        catch (Exception e) {
            client.close();
            return new ScanResult(false, "Failed to write new files");
        }

        return new ScanResult(true, "");
    }

    private static List<File> scanNewFiles(DiskShare diskShare, String shareName, String sourceFolder) {
        File phoneFileFolder = new File(sourceFolder);
        List<File> phoneFiles = Arrays.stream(phoneFileFolder.listFiles())
                .filter(File::isFile).collect(Collectors.toList());
        List<File> newFiles = new LinkedList<>();
        // Find filenames not yet uploaded
        for (File file : phoneFiles) {
            if (!diskShare.fileExists(shareName + file.getName())) {
                System.out.println("Found new file: " + file.getName());
                newFiles.add(file);
            }
        }
        return newFiles;
    }

    private static void writeToFile(DiskShare diskShare, File file, String shareFolder) throws IOException {

        byte[] fileContent = Files.readAllBytes(file.toPath());

        Set<FileAttributes> fileAttributes = new HashSet<>();
        fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
        Set<SMB2CreateOptions> createOptions = new HashSet<>();
        createOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS);

        com.hierynomus.smbj.share.File f = diskShare.openFile(shareFolder + file.getName(),
                new HashSet(Collections.singleton(AccessMask.GENERIC_ALL)), fileAttributes, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_CREATE, createOptions);

        System.out.println("Writing file: " + file.getName());
        f.write(fileContent, 0);
    }
}
