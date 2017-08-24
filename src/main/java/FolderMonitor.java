import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;

public class FolderMonitor implements Runnable {

    private Path folder;
    private File folderFile;
    private HashMap<Object, Game> games;

    public FolderMonitor(String folder) throws IllegalArgumentException {

        if (!Files.isDirectory(Paths.get(folder))) {
            throw new IllegalArgumentException(String.format("Folder %s doesn't exist", folder));
        }
        this.folder = Paths.get(folder);
        this.folderFile = new File(folder);
        this.games = new HashMap<>();
    }

    private void triggerExistingFiles() {

        for (File fileEntry : folderFile.listFiles()) {
            if (fileEntry.isFile()) {
                fileEntry.setLastModified(fileEntry.lastModified());
            }
        }
    }

    @Override
    public void run() {

        try (WatchService watch = FileSystems.getDefault().newWatchService()) {
            folder.register(watch, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey key;
            triggerExistingFiles();
            while ((key = watch.take()) != null) {
                for (WatchEvent e : key.pollEvents()) {
                    String path = folder.toAbsolutePath() + "\\" + e.context().toString();
                    if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE || e.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
                        System.out.println(games.containsKey(e.context()));
                        if (!games.containsKey(e.context())) {
                            Game game = new Game(path);
                            games.put(e.context(), game);
                            new Thread(game).start();
                        }
                    }
                    else if(e.kind() == StandardWatchEventKinds.ENTRY_DELETE && games.containsKey(e.context())){
                        games.remove(e.context());
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}