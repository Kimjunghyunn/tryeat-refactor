package shop.tryit.domain.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ItemFileStore {

    @Value("${custom.path.upload-images}")
    private String filePath;

    public String getFullPath(String fileName) {
        return filePath + fileName;
    }

    public ItemFile storeMainImage(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originFileName = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originFileName);
        multipartFile.transferTo(new File(getFullPath(storeFileName)));
        return ItemFile.of(originFileName, storeFileName);
    }

    public List<ItemFile> storeDetailImages(List<MultipartFile> multipartFiles) throws IOException {
        List<ItemFile> storeDetailImagesResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeDetailImagesResult.add(storeMainImage(multipartFile));
            }
        }
        return storeDetailImagesResult;
    }

    /**
     * storeFileName 생성
     * ex) image.png -> uuid.png
     */
    private String createStoreFileName(String originFileName) {
        String ext = extracted(originFileName); // 파일 확장자
        String uuid = UUID.randomUUID().toString(); // 서버에 저장될 이름
        return uuid + "." + ext;
    }

    /**
     * 파일 확장자 추출
     */
    private String extracted(String originFileName) {
        int pos = originFileName.lastIndexOf(".");
        return originFileName.substring(pos + 1);
    }

}
