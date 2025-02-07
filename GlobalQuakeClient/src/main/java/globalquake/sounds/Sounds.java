package globalquake.sounds;

import globalquake.core.GlobalQuake;
import globalquake.core.exception.FatalIOException;
import globalquake.core.Settings;
import org.tinylog.Logger;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sounds {

	private static final File EXPORT_DIR = new File(GlobalQuake.mainFolder, "sounds/");
	public static Clip level_0;
	public static Clip level_1;
	public static Clip level_2;
	public static Clip level_3;
	public static Clip level_4;
	public static Clip intensify;
	public static Clip felt;
	public static Clip eew_warning;
	public static Clip felt_strong;
	public static Clip countdown;
	public static Clip countdown2;
	public static Clip update;

	public static Clip found;

	public static boolean soundsAvailable = true;

	private static final ExecutorService soundService = Executors.newCachedThreadPool();

	private static Clip loadSound(String res) throws FatalIOException {
		try {
			Path soundPath = Paths.get(EXPORT_DIR.getAbsolutePath(), res);
			InputStream audioInStream = Files.exists(soundPath) ?
					new FileInputStream(soundPath.toFile()) :
					ClassLoader.getSystemClassLoader().getResourceAsStream("sounds/" + res);

			if (audioInStream == null) {
				throw new IOException("Sound file not found: " + res);
			}

			AudioInputStream audioIn = AudioSystem.getAudioInputStream(
					new BufferedInputStream(audioInStream));
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			return clip;
		} catch(Exception e) {
			soundsAvailable = false;
			throw new FatalIOException("Failed to load sound: " + res, e);
		}
	}

	public static void exportSounds() throws IOException {
		Path exportPath = Paths.get(EXPORT_DIR.getAbsolutePath());
		if (!Files.exists(exportPath)) {
			Files.createDirectory(exportPath);
			writeReadmeFile(exportPath);
		}

		String[] soundFiles = {"level_0.wav", "level_1.wav", "level_2.wav", "level_3.wav", "level_4.wav",
				"intensify.wav", "felt.wav", "felt_strong.wav", "eew_warning.wav", "countdown.wav", "update.wav", "found.wav"};

		for (String soundFile : soundFiles) {
			Path exportedFilePath = exportPath.resolve(soundFile);
			if (!Files.exists(exportedFilePath)) { // Check if the file already exists
				InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sounds/" + soundFile);
				if (is != null) {
					Files.copy(is, exportedFilePath, StandardCopyOption.REPLACE_EXISTING);
					is.close();
				}
			}
		}
	}

	private static void writeReadmeFile(Path exportPath) throws IOException {
		String readmeContent = """
                README

                This directory contains the exported sound files from GlobalQuake.
                You can edit these sound files as per your preference.
                Please note that the sounds will only be exported once, meaning that any changes you make here will be kept and used by GlobalQuake.
                After uploading your sounds, please restart GlobalQuake.

                Enjoy customizing your sound experience!""";

		Files.writeString(exportPath.resolve("README.txt"), readmeContent, StandardOpenOption.CREATE);
	}


	public static void load() throws Exception {
		exportSounds();

		// CLUSTER
		level_0 = loadSound("level_0.wav");
		level_1 = loadSound("level_1.wav");
		level_2 = loadSound("level_2.wav");
		level_3 = loadSound("level_3.wav");
		level_4 = loadSound("level_4.wav");

		// QUAKE
		found = loadSound("found.wav");
		update = loadSound("update.wav");

		// LOCAL
		intensify = loadSound("intensify.wav");
		felt = loadSound("felt.wav");
		felt_strong = loadSound("felt_strong.wav");
		eew_warning = loadSound("eew_warning.wav");
		// strong_felt ?

		// ???
		countdown = loadSound("countdown.wav");
		countdown2 = loadSound("countdown.wav");
	}

	public static void playSound(Clip clip) {
		if(!Settings.enableSound || !soundsAvailable || clip == null) {
			return;
		}

		soundService.submit(() -> {
			try {
				playClipRuntime(clip);
			} catch(Exception e){
				Logger.error(e);
			}
		});
	}

	private static void playClipRuntime(Clip clip) {
		clip.stop();
		clip.flush();
		clip.setFramePosition(0);
		clip.start();

        try {
            Thread.sleep(clip.getMicrosecondLength() / 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
	}

	public static void main(String[] args) throws Exception {
		GlobalQuake.prepare(new File("."), null);
		load();

		playSound(level_2);

		Thread.sleep(3000);

		System.exit(0);
	}

}
