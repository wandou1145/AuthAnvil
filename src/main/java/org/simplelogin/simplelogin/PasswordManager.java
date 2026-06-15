package org.simplelogin.simplelogin;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordManager {

    public String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }

    public String validatePassword(String password, ConfigManager config) {
        if (password.length() < config.getPasswordMinLength()) {
            return "§c密码长度不能少于" + config.getPasswordMinLength() + "个字符";
        }
        if (password.length() > config.getPasswordMaxLength()) {
            return "§c密码长度不能超过" + config.getPasswordMaxLength() + "个字符";
        }
        if (config.isPasswordRequireLetter() && !password.matches(".*[a-zA-Z].*")) {
            return "§c密码必须包含至少一个字母";
        }
        if (config.isPasswordRequireNumber() && !password.matches(".*\\d.*")) {
            return "§c密码必须包含至少一个数字";
        }
        return null;
    }
}