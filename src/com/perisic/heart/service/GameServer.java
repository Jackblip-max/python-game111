package com.perisic.heart.service;

import com.perisic.heart.model.Game;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Base64;
import javax.imageio.ImageIO;

public class GameServer {
    
    private String readUrl(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            InputStream inputStream = url.openStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (Exception e) {
            System.out.println("Error reading URL: " + e.getMessage());
            return null;
        }
    }
    
    public Game getRandomGame() {
        String apiUrl = "https://marcconrad.com/uob/heart/api.php?out=csv&base64=yes";
        String data = readUrl(apiUrl);
        
        if (data == null) return null;
        
        String[] parts = data.split(",");
        byte[] imageBytes = Base64.getDecoder().decode(parts[0]);
        int solution = Integer.parseInt(parts[1].trim());
        
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            return new Game(image, solution);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}