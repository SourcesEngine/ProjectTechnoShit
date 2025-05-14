package com.example.hibyassistant.aitools;

import androidx.annotation.RequiresApi;

import android.annotation.SuppressLint;
import android.os.Build;
import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

public class MilkColorResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("result")
    private Result result;

    public String getStatus() {
        return status;
    }

    public Result getResult() {
        return result;
    }

    public static class Result {
        @SerializedName("tags")
        private Tag[] tags;

        @SerializedName("colors")
        private ColorInfo[] colors;

        public Tag[] getTags() {
            return tags;
        }

        public ColorInfo[] getColors() {
            return colors;
        }
    }

    public static class Tag {
        @SerializedName("confidence")
        private double confidence;

        @SerializedName("tag")
        private TagInfo tag;

        public double getConfidence() {
            return confidence;
        }

        public TagInfo getTag() {
            return tag;
        }
    }

    public static class TagInfo {
        @SerializedName("en")
        private String english;

        public String getEnglish() {
            return english;
        }
    }

    public static class ColorInfo {
        @SerializedName("r")
        private int red;

        @SerializedName("g")
        private int green;

        @SerializedName("b")
        private int blue;

        @SerializedName("percent")
        private double percent;

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }

        public double getPercent() {
            return percent;
        }

        public String getColorName() {
            return getColorNameFromRGB(red, green, blue);
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public String analyzeLiquidForBaby() {
        if (result == null || result.getColors() == null) {
            return "Unable to analyze the liquid color. Please try again.";
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("🔍 Liquid Color Analysis:\n\n");

        // Sort colors by percentage
        List<ColorInfo> sortedColors = Arrays.stream(result.getColors())
                .sorted((c1, c2) -> Double.compare(c2.getPercent(), c1.getPercent()))
                .toList();

        // Get dominant colors (top 3)
        List<ColorInfo> dominantColors = sortedColors.subList(0, 
                Math.min(3, sortedColors.size()));

        // Analyze dominant colors
        analysis.append("🎨 Dominant Colors:\n");
        for (ColorInfo color : dominantColors) {
            String colorName = color.getColorName();
            analysis.append(String.format("- %s (%.1f%%)\n", 
                    colorName, color.getPercent()));
        }
        analysis.append("\n");

        // Color Safety Assessment
        analysis.append("⚠️ Safety Assessment:\n");
        boolean isSafe = analyzeColorSafety(dominantColors);
        if (isSafe) {
            analysis.append("✅ The liquid appears to be in good condition.\n");
        } else {
            analysis.append("❌ The liquid may be unsafe for consumption.\n");
        }
        analysis.append("\n");

        // Detailed Color Analysis
        analysis.append("📊 Color Details:\n");
        for (ColorInfo color : dominantColors) {
            analysis.append(String.format("- RGB: (%d, %d, %d)\n", 
                    color.getRed(), color.getGreen(), color.getBlue()));
        }
        analysis.append("\n");

        // Recommendations
        analysis.append("💡 Recommendations:\n");
        analysis.append(getColorBasedRecommendations(dominantColors, isSafe));

        return analysis.toString();
    }

    private static String getColorNameFromRGB(int r, int g, int b) {
        // Define color ranges
        if (isWhite(r, g, b)) return "White";
        if (isCream(r, g, b)) return "Cream";
        if (isYellow(r, g, b)) return "Yellow";
        if (isBrown(r, g, b)) return "Brown";
        if (isGreen(r, g, b)) return "Green";
        if (isBlue(r, g, b)) return "Blue";
        if (isPink(r, g, b)) return "Pink";
        if (isGray(r, g, b)) return "Gray";
        return "Unknown";
    }

    private static boolean isWhite(int r, int g, int b) {
        return r > 200 && g > 200 && b > 200;
    }

    private static boolean isCream(int r, int g, int b) {
        return r > 240 && g > 230 && b > 200;
    }

    private static boolean isYellow(int r, int g, int b) {
        return r > 200 && g > 200 && b < 100;
    }

    private static boolean isBrown(int r, int g, int b) {
        return r > 150 && g < 100 && b < 100;
    }

    private static boolean isGreen(int r, int g, int b) {
        return r < 100 && g > 150 && b < 100;
    }

    private static boolean isBlue(int r, int g, int b) {
        return r < 100 && g < 100 && b > 150;
    }

    private static boolean isPink(int r, int g, int b) {
        return r > 200 && g < 150 && b > 150;
    }

    private static boolean isGray(int r, int g, int b) {
        int diff = Math.max(Math.abs(r - g), Math.abs(g - b));
        return diff < 30 && r < 200 && g < 200 && b < 200;
    }

    private boolean analyzeColorSafety(List<ColorInfo> colors) {
        // Check if the dominant color is safe
        ColorInfo dominantColor = colors.get(0);
        String colorName = dominantColor.getColorName();
        
        // Safe colors for milk
        return colorName.equals("White") || 
               colorName.equals("Cream") || 
               (colorName.equals("Yellow") && dominantColor.getPercent() < 20);
    }

    private String getColorBasedRecommendations(List<ColorInfo> colors, boolean isSafe) {
        StringBuilder recommendations = new StringBuilder();
        ColorInfo dominantColor = colors.get(0);
        String colorName = dominantColor.getColorName();

        switch (colorName) {
            case "White":
                recommendations.append("- This appears to be fresh milk\n");
                recommendations.append("- Safe for baby consumption\n");
                break;
            case "Cream":
                recommendations.append("- This appears to be slightly aged milk\n");
                recommendations.append("- Still safe for consumption\n");
                recommendations.append("- Check expiration date\n");
                break;
            case "Yellow":
                if (dominantColor.getPercent() < 20) {
                    recommendations.append("- Slight yellow tint detected\n");
                    recommendations.append("- May be starting to spoil\n");
                    recommendations.append("- Not recommended for babies\n");
                } else {
                    recommendations.append("- Strong yellow color detected\n");
                    recommendations.append("- Likely spoiled\n");
                    recommendations.append("- DO NOT feed to babies\n");
                }
                break;
            case "Brown":
                recommendations.append("- Brown color indicates spoilage\n");
                recommendations.append("- DO NOT feed to babies\n");
                recommendations.append("- Dispose of the milk\n");
                break;
            case "Green":
                recommendations.append("- Green color indicates bacterial growth\n");
                recommendations.append("- DO NOT feed to babies\n");
                recommendations.append("- Dispose of the milk immediately\n");
                break;
            default:
                recommendations.append("- Unable to determine milk quality\n");
                recommendations.append("- Please consult with a healthcare provider\n");
                break;
        }

        return recommendations.toString();
    }
} 