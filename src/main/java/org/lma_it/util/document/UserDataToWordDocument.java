package org.lma_it.util.document;

import ch.qos.logback.classic.Logger;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import org.lma_it.model.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class UserDataToWordDocument {

    private final Logger logger;

    private static final String TEMP_IMAGE_PATH = "resources/photo.jpg";

    public UserDataToWordDocument(Logger logger){
        this.logger = logger;
    }

    public File generateUserDocument(User user, Logger logger, File templateFile, byte[] image) {

        File outputFile = new File("User_" + user.getName() + ".docx");

        try (FileInputStream fis = new FileInputStream(templateFile);
             XWPFDocument document = new XWPFDocument(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String text = run.getText(0);
                    if (text != null) {
                        text = text.replace("userSecondName", user.getSecondName());
                        text = text.replace("userName", user.getName());
                        text = text.replace("userLastName", user.getLastName() != null ? user.getLastName() : "****");
                        text = text.replace("userBirthDate", user.getBirthDate().toString());
                        text = text.replace("userGender", user.getGender());
                        run.setText(text, 0);
                    }
                }
            }

            saveImageFromBytes(image, TEMP_IMAGE_PATH);

            if (!document.getTables().isEmpty()) {
                XWPFTable table = document.getTables().get(0);
                XWPFTableRow row = table.getRow(0);
                XWPFTableCell cell = row.getCell(0);

                if (cell.getText().trim().equalsIgnoreCase("фото")) {
                    insertImageIntoCell(cell, TEMP_IMAGE_PATH);
                }
            }

            document.write(fos);

            new File(TEMP_IMAGE_PATH).delete();

        } catch (Exception e) {
            logger.error("Ошибка при создании файла для пользователя. Причина: {}", e.getMessage());
        }

        return outputFile;
    }


    private void insertImageIntoCell(XWPFTableCell cell, String imagePath) {
        try {
            cell.removeParagraph(0);
            XWPFParagraph imageParagraph = cell.addParagraph();
            XWPFRun imageRun = imageParagraph.createRun();

            InputStream imageStream = new FileInputStream(imagePath);
            imageRun.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_JPEG, imagePath,
                    Units.toEMU(105), Units.toEMU(150));
            imageStream.close();
        } catch (Exception e) {
            logger.error("Ошибка при замене фотографии в методе insertImageIntoCell. Причина: {}", e.getMessage());
        }
    }

    private void saveImageFromBytes(byte[] imageData, String filePath) {
        try{
            logger.info("Размер массива imageData он же image: {}", imageData.length);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
            File outputFile = new File(filePath);
            outputFile.getParentFile().mkdirs();
            ImageIO.write(img, "jpg", outputFile);
        }catch (IOException e){
            logger.error("Ошибка при сохранении файла в папку. Причина: {}", e.getMessage());
        }

    }

}
