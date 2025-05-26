package BLL.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PBKDF2PasswordUtilTest {

    @Test
    void testHashAndVerifyPassword() throws Exception {
        //assign and act

        //standard password
        String password = "password1!";
        String hash1 = PBKDF2PasswordUtil.hashPassword(password);
        //long password
        String longPassword = "111222333444555666777888999101010";
        String hash2 = PBKDF2PasswordUtil.hashPassword(longPassword);
        //weird password
        String weirdPassword = "!(/#/(&!(#/=ååøæææøå()!#)(!";
        String hash3 = PBKDF2PasswordUtil.hashPassword(weirdPassword);

        //assert
        assertTrue(PBKDF2PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PBKDF2PasswordUtil.verifyPassword(longPassword, hash2));
        assertTrue(PBKDF2PasswordUtil.verifyPassword(weirdPassword, hash3));
    }

    @Test
    void testWrongPasswordFail() throws Exception {
        //assign and act
        String password = "123";
        String wrongPassword = "321";
        String hashed = PBKDF2PasswordUtil.hashPassword(password);

        //assert
        assertFalse(PBKDF2PasswordUtil.verifyPassword(wrongPassword, hashed));
    }

    @Test
    void testUniqueness() throws Exception {
        //assign and act
        String password = "same";
        String hash1 = PBKDF2PasswordUtil.hashPassword(password);
        String hash2 = PBKDF2PasswordUtil.hashPassword(password);

        //assert
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testNullOrEmptyPassword() {
        assertThrows(Exception.class, () -> PBKDF2PasswordUtil.hashPassword(null));
        assertThrows(Exception.class, () -> PBKDF2PasswordUtil.verifyPassword("", ""));
    }
}