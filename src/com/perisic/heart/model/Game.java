package com.perisic.heart.model;

import java.awt.image.BufferedImage;

public class Game {
    private BufferedImage image;
    private int solution;
    
    public Game(BufferedImage image, int solution) {
        this.image = image;
        this.solution = solution;
    }
    
    public BufferedImage getImage() { 
        return image; 
    }
    
    public int getSolution() { 
        return solution; 
    }
}