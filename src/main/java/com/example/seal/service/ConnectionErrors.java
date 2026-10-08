package com.example.seal.service;

import java.security.Security;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

/** Safe UI messages; never includes request bodies, tokens, or credentials. */
public final class ConnectionErrors {
    private ConnectionErrors() { }

    public static String message(Throwable error) {
        if (Security.getProvider("SunEC") == null) {
            return "This app runtime is missing TLS support. Download and extract the latest Windows build.";
        }
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof CertificateExpiredException || cause instanceof CertificateNotYetValidException) {
                return "HTTPS certificate date validation failed. Check your computer's date and time; the server certificate may also need renewal.";
            }
            if (cause instanceof CertPathBuilderException || cause instanceof CertPathValidatorException) {
                return "The server certificate could not be verified. Use the latest app build and run Check Server Connection.bat for diagnostics.";
            }
        }
        return "Secure connection failed (TLS handshake). Run Check Server Connection.bat and share its diagnostic output.";
    }
}
