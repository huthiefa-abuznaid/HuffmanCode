package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Driver {

    public static void main(String[] args) {
        System.out.println("=== البدء في فحص مشروع Huffman ===");
        
        // 1. استخدام مسارات مطلقة (Absolute Paths) لمنع أي مشاكل مع getParent()
        File originalFile = new File("test_original.txt").getAbsoluteFile();
        File compressedFile = new File("test_compressed.huf").getAbsoluteFile();
        
        // بناءً على تعديل دالة decode الجديد: 
        // الملف المفكوك سيكون بنفس اسم الملف المضغوط (test_compressed) ولكن بالامتداد الأصلي (.txt)
        File decompressedFile = new File(compressedFile.getParent(), "test_compressed.txt"); 

        try {
            // تنظيف المجلد قبل البدء بالفحص
            if (originalFile.exists()) originalFile.delete();
            if (compressedFile.exists()) compressedFile.delete();
            if (decompressedFile.exists()) decompressedFile.delete();

            // كتابة النص الاختباري
            FileWriter writer = new FileWriter(originalFile);
            writer.write("aaaaabbbbcccdde\nHello Huffman Project 2026! @#$");
            writer.close();
            System.out.println("✔ تم إنشاء ملف الاختبار الأصلي بنجاح.");

            // 2. فحص عملية الضغط (Compression)
            HuffmanTreeCode engine = new HuffmanTreeCode();
            System.out.println("جاري ضغط الملف...");
            engine.setCompressTarget(originalFile, compressedFile.getAbsolutePath());
            engine.compress();
            
            System.out.println("✔ تمت عملية الضغط.");
            System.out.println("حجم الملف الأصلي: " + originalFile.length() + " بايت");
            System.out.println("حجم الملف المضغوط: " + compressedFile.length() + " بايت");
            System.out.println("نسبة الضغط: " + engine.getCompressionRatioFormatted());

            // 3. فحص عملية فك الضغط (Decompression)
            System.out.println("جاري فك ضغط الملف...");
            HuffmanTreeCode decompressEngine = new HuffmanTreeCode();
            
            // قراءة الملف وتمريره للدالة المعدلة بالداخل
            decompressEngine.readCompressedFile(compressedFile, null);
            
            // 4. التحقق من تطابق الملفات (Validation)
            System.out.println("جاري التحقق من مطابقة بايتات الملف الناتج...");
            
            if (decompressedFile.exists()) {
                byte[] originalBytes = Files.readAllBytes(originalFile.toPath());
                byte[] decompressedBytes = Files.readAllBytes(decompressedFile.toPath());
                
                boolean isIdentical = java.util.Arrays.equals(originalBytes, decompressedBytes);
                
                if (isIdentical) {
                    System.out.println("\n⭐⭐⭐⭐⭐ النتيجة: نجاح باهر! ⭐⭐⭐⭐⭐");
                    System.out.println("✔ الملف المفكوك متطابق تماماً بنسبة 100% مع الملف الأصلي.");
                } else {
                    System.out.println("\n❌ فشل الاختبار: الملف الناتج لا يطابق الملف الأصلي (بيانات تالفة).");
                }
            } else {
                System.out.println("❌ فشل الاختبار: لم يتم العثور على الملف المفكوك.");
                System.out.println("كان من المتوقع وجوده في المسار: " + decompressedFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.out.println("❌ حدث خطأ أثناء الاختبار: " + e.getMessage());
            e.printStackTrace();
        }
    }
}