package com.example.android_nas_sync.common;
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
    static String ipAddress = "192.168.0.45";
    static String username = "file-sync";
    static String password = "file-sync";
    static String shareName = "backup";
    static String shareFolder = "phone_pixel/camera/";
    static String phoneFolder = "src/img";
    private void doScan(){
        SMBClient client = new SMBClient();
        try {
            Connection connection = client.connect(ipAddress);
            AuthenticationContext authContext = new AuthenticationContext(username, password.toCharArray(), ipAddress);
            Session session = connection.authenticate(authContext);
            DiskShare share = (DiskShare) session.connectShare(shareName);

            List<File> newFiles = scanNewFiles(share);
            for (File file : newFiles) {
                writeToFile(share, file);

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        client.close();
    }
    private static List<File> scanNewFiles(DiskShare diskShare) {
        File phoneFileFolder = new File(phoneFolder);
        List<File> phoneFiles = Arrays.stream(phoneFileFolder.listFiles())
                .filter(File::isFile).collect(Collectors.toList());
        List<File> newFiles = new LinkedList<>();
        // Find filenames not yet uploaded
        for (File file : phoneFiles) {
            if (!diskShare.fileExists(shareFolder + file.getName())) {
                System.out.println("Found new file: " + file.getName());
                newFiles.add(file);
            }
        }
        return newFiles;
    }

    private static void writeToFile(DiskShare diskShare, File file) throws IOException {

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
