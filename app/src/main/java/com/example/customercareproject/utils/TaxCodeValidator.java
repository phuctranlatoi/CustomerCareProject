package com.example.customercareproject.utils;

/**
 * Validator cho mã số thuế Việt Nam
 * - MST 10 số: 9 số + 1 chữ số kiểm tra
 * - MST 13 số: 10 số (MST gốc) + 3 số mã chi nhánh
 */
public class TaxCodeValidator {

    /**
     * Kiểm tra mã số thuế có hợp lệ không
     * @param taxCode Mã số thuế cần kiểm tra
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValid(String taxCode) {
        if (taxCode == null || taxCode.isEmpty()) return false;
        
        // Chỉ chấp nhận số
        if (!taxCode.matches("\\d+")) return false;
        
        int length = taxCode.length();
        
        if (length == 10) {
            return validateTenDigit(taxCode);
        } else if (length == 13) {
            return validateThirteenDigit(taxCode);
        }
        
        return false;
    }

    /**
     * Validate MST 10 số
     * Công thức: checkDigit = (31 - (c1×31 + c2×29 + c3×27 + ... + c9×11) mod 11) mod 10
     */
    private static boolean validateTenDigit(String taxCode) {
        try {
            int[] weights = {31, 29, 27, 25, 23, 21, 19, 17, 15, 13, 11};
            int sum = 0;
            
            for (int i = 0; i < 9; i++) {
                int digit = Character.getNumericValue(taxCode.charAt(i));
                sum += digit * weights[i];
            }
            
            int calculatedCheck = (31 - (sum % 11)) % 10;
            int actualCheck = Character.getNumericValue(taxCode.charAt(9));
            
            return calculatedCheck == actualCheck;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate MST 13 số
     * - 10 số đầu phải là MST hợp lệ
     * - 3 số cuối là mã chi nhánh (001-999)
     */
    private static boolean validateThirteenDigit(String taxCode) {
        try {
            // Kiểm tra 10 số đầu
            String baseTaxCode = taxCode.substring(0, 10);
            if (!validateTenDigit(baseTaxCode)) return false;
            
            // Kiểm tra 3 số cuối (mã chi nhánh phải từ 001-999)
            String branchCode = taxCode.substring(10, 13);
            int branch = Integer.parseInt(branchCode);
            return branch >= 1 && branch <= 999;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy thông báo lỗi chi tiết
     */
    public static String getErrorMessage(String taxCode) {
        if (taxCode == null || taxCode.isEmpty()) {
            return "Mã số thuế không được để trống";
        }
        
        if (!taxCode.matches("\\d+")) {
            return "Mã số thuế chỉ được chứa số";
        }
        
        int length = taxCode.length();
        
        if (length != 10 && length != 13) {
            return "Mã số thuế phải có 10 hoặc 13 số";
        }
        
        if (length == 10 && !validateTenDigit(taxCode)) {
            return "Mã số thuế 10 số không hợp lệ (sai chữ số kiểm tra)";
        }
        
        if (length == 13) {
            String baseTaxCode = taxCode.substring(0, 10);
            if (!validateTenDigit(baseTaxCode)) {
                return "Mã số thuế 13 số không hợp lệ (10 số đầu sai chữ số kiểm tra)";
            }
            
            try {
                String branchCode = taxCode.substring(10, 13);
                int branch = Integer.parseInt(branchCode);
                if (branch < 1 || branch > 999) {
                    return "Mã chi nhánh (3 số cuối) phải từ 001-999";
                }
            } catch (Exception e) {
                return "Mã chi nhánh không hợp lệ";
            }
        }
        
        return "Mã số thuế hợp lệ";
    }
}
