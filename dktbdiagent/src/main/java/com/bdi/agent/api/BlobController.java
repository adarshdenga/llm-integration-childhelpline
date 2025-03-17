//package com.bdi.agent.api;
//
//import com.azure.storage.blob.BlobServiceAsyncClient;
//import com.azure.storage.blob.BlobServiceClientBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.WritableResource;
//import org.springframework.util.StreamUtils;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.charset.Charset;
//
//@RestController
//@RequestMapping("blob")
//public class BlobController {
//
//    @Autowired
//    private BlobServiceClientBuilder blobServiceClientBuilder;
//
//    private final BlobServiceAsyncClient blobServiceAsyncClient = blobServiceClientBuilder.buildAsyncClient();
//
//    @Value("azure-blob://bdi/{blobName}")
//    private Resource blobFile;
//
//    @GetMapping("/readBlobFile")
//    public String readBlobFile() throws IOException {
//        return StreamUtils.copyToString(
//                this.blobFile.getInputStream(),
//                Charset.defaultCharset());
//    }
//
//    @PostMapping("/writeBlobFile")
//    public String writeBlobFile(@RequestBody String data) throws IOException {
//        try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
//            os.write(data.getBytes());
//        }
//        return "file was updated";
//    }
//}