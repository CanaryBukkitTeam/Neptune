/*
 * This file is part of NeptuneVanilla, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2016, Jamie Mansfield <https://github.com/jamierocks>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.neptunepowered.vanilla.launch;

import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NeptuneServerMain {

    private static final String LIBRARIES_DIR = "libraries";

    private static final String MINECRAFT_SERVER_LOCAL = "minecraft_server.1.8.9.jar";
    private static final String MINECRAFT_SERVER_REMOTE = "https://s3.amazonaws.com/Minecraft.Download/versions/1.8.9/minecraft_server.1.8.9.jar";

    private static final String LAUNCHWRAPPER_PATH = "/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar";
    private static final String LAUNCHWRAPPER_LOCAL = LIBRARIES_DIR + LAUNCHWRAPPER_PATH;
    private static final String LAUNCHWRAPPER_REMOTE = "https://libraries.minecraft.net" + LAUNCHWRAPPER_PATH;

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private NeptuneServerMain() {
    }

    public static void main(String[] args) throws Exception {
        // Get the location of our jar
        Path base = Paths.get(NeptuneServerMain.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

        if (!checkMinecraft(base)) {
            return;
        }

        Launch.main(join(args,
                "--tweakClass", "org.neptunepowered.vanilla.launch.NeptuneServerTweaker"
        ));
    }

    private static String[] join(String[] args, String... prefix) {
        String[] result = new String[prefix.length + args.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(args, 0, result, prefix.length, args.length);
        return result;
    }

    private static boolean checkMinecraft(Path base) throws Exception {
        // Make sure the Minecraft server is available, or download it otherwise
        Path path = base.resolve(MINECRAFT_SERVER_LOCAL);
        if (Files.notExists(path) && !downloadVerified(MINECRAFT_SERVER_REMOTE, path)) {
            return false;
        }

        // Make sure Launchwrapper is available, or download it otherwise
        path = base.resolve(LAUNCHWRAPPER_LOCAL);
        return Files.exists(path) || downloadVerified(LAUNCHWRAPPER_REMOTE, path);
    }

    private static boolean downloadVerified(String remote, Path path) throws IOException, NoSuchAlgorithmException {
        Files.createDirectories(path.getParent());

        String name = path.getFileName().toString();
        URL url = new URL(remote);

        System.out.println("Downloading " + name + "... This can take a while.");
        System.out.println(url);
        URLConnection con = url.openConnection();
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        try (ReadableByteChannel source = Channels.newChannel(new DigestInputStream(con.getInputStream(), md5));
                FileChannel out = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            out.transferFrom(source, 0, Long.MAX_VALUE);
        }

        String expected = getETag(con);
        if (!expected.isEmpty()) {
            String hash = toHexString(md5.digest());
            if (hash.equals(expected)) {
                System.out.println("Successfully downloaded " + name + " and verified checksum!");
            } else {
                Files.delete(path);
                throw new IOException("Checksum verification failed: Expected " + expected + ", got " + hash);
            }
        }

        return true;
    }

    private static String getETag(URLConnection con) {
        String hash = con.getHeaderField("ETag");
        if (hash == null || hash.isEmpty()) {
            return "";
        }

        if (hash.startsWith("\"") && hash.endsWith("\"")) {
            hash = hash.substring(1, hash.length() - 1);
        }

        return hash;
    }

    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}