import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

public class CloudFunctionsForJava implements HttpFunction {

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException, InterruptedException {

    // ストリーミング WRITE
    // 参考URL
    // https://cloud.google.com/storage/docs/streaming#storage-stream-upload-object-java
    String projectId = "xxxxx";
    String bucketName = "xxxxx";
    String objectName = "write-file.txt";
    String contents = "Hello world!\r\n";
    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    BlobId blobId = BlobId.of(bucketName, objectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
    byte[] content = contents.getBytes(StandardCharsets.UTF_8);
    try (WriteChannel writer = storage.writer(blobInfo)) {
      for (int i = 0; i < 10; i++) {
        writer.write(ByteBuffer.wrap(content));
      }
    }

    // ストリーミング READし、ファイル出力
    // 参考URL
    // https://cloud.google.com/storage/docs/streaming#storage-stream-upload-object-java
    BlobId blobId2 = BlobId.of(bucketName, "CsvSample1_copy.csv");
    BlobInfo blobInfo2 = BlobInfo.newBuilder(blobId2).setContentType("text/csv").build();
    try (WriteChannel writer = storage.writer(blobInfo2)) {
      blobId = BlobId.of(bucketName, "CsvSample1.csv");
      try (ReadChannel reader = storage.reader(blobId)) {
          ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
          while (reader.read(bytes) > 0) {
              bytes.flip();
              writer.write(bytes);
              bytes.clear();
          }
        }
    }

    // ファイル結合のサンプル
    // 参考URL
    // https://cloud.google.com/storage/docs/samples/storage-compose-file
    String firstObjectName = "CsvSample1.csv";
    String secondObjectName = "CsvSample2.csv";
    String targetObjectName = "new-composite-CsvSample.csv";
    Storage storage3 = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

    Storage.ComposeRequest composeRequest =
        Storage.ComposeRequest.newBuilder()
            .addSource(firstObjectName, secondObjectName)
            .setTarget(BlobInfo.newBuilder(bucketName, targetObjectName).setContentType("text/csv").build())
            .build();
    Blob compositeObject = storage3.compose(composeRequest);
    System.out.println(
      "New composite object "
          + compositeObject.getName()
          + " was created by combining "
          + firstObjectName
          + " and "
          + secondObjectName);

    // レスポンス返却
    response.setContentType("text/plain; charset=utf-8");
    var writer = response.getWriter();
    writer.write("OK");
  }
}