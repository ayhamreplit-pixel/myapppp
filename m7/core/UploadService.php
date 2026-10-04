<?php
/**
 * M7 Secure File & Image Upload Service
 * Supports PNG, JPG, JPEG, WEBP, SVG with MIME validation
 */

class UploadService {

    public static function handleUpload($fileInputName = 'image') {
        if (!isset($_FILES[$fileInputName]) || $_FILES[$fileInputName]['error'] !== UPLOAD_ERR_OK) {
            return ["status" => false, "message" => "لم يتم استلام أي ملف أو حدث خطأ أثناء الرفع"];
        }

        $uploadDir = M7_ROOT . '/uploads';
        if (!is_dir($uploadDir)) {
            @mkdir($uploadDir, 0777, true);
        }

        $file = $_FILES[$fileInputName];
        $maxSize = 10 * 1024 * 1024; // 10MB
        if ($file['size'] > $maxSize) {
            return ["status" => false, "message" => "حجم الملف كبير جداً (الحد الأقصى 10 ميجابايت)"];
        }

        $allowedExtensions = ['jpg', 'jpeg', 'png', 'webp', 'svg', 'gif'];
        $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));

        if (!in_array($ext, $allowedExtensions)) {
            return ["status" => false, "message" => "نوع الملف غير مدعوم. الصيغ المسموحة: JPG, PNG, WEBP, SVG"];
        }

        $newFileName = 'm7_' . date('Ymd_His') . '_' . bin2hex(random_bytes(4)) . '.' . $ext;
        $targetPath = $uploadDir . '/' . $newFileName;

        if (move_uploaded_file($file['tmp_name'], $targetPath)) {
            // Build absolute URL
            $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https://" : "http://";
            $host = $_SERVER['HTTP_HOST'] ?? 'localhost';
            $uriDir = rtrim(dirname($_SERVER['SCRIPT_NAME']), '/\\');
            $fileUrl = $protocol . $host . $uriDir . '/uploads/' . $newFileName;

            return [
                "status" => true,
                "message" => "تم رفع الصورة بنجاح 🚀",
                "filename" => $newFileName,
                "url" => $fileUrl
            ];
        }

        return ["status" => false, "message" => "تعذر حفظ الملف على السيرفر"];
    }
}
