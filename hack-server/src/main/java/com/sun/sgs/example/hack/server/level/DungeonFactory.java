/*
 * This work is hereby released into the Public Domain. 
 * To view a copy of the public domain dedication, visit 
 * http://creativecommons.org/licenses/publicdomain/ or send 
 * a letter to Creative Commons, 171 Second Street, Suite 300, 
 * San Francisco, California, 94105, USA.
 */

package com.sun.sgs.example.hack.server.level;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;

import com.sun.sgs.example.hack.server.Game;
import com.sun.sgs.example.hack.server.GameConnector;

import com.sun.sgs.example.hack.server.ai.AICharacterManager;
import com.sun.sgs.example.hack.server.ai.MonsterFactory;
import com.sun.sgs.example.hack.server.ai.NPCharacter;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import java.util.logging.Logger;

/**
 * This is a fairly simple static factory class that handles loading
 * the contents of a single <code>Dungeon</code>. All AI, levels,
 * etc. are correctly registered as part of the process.
 *
 * <p>
 *
 * Note that in a richer app this would be an interface and provide a
 * way to define multiple factories. These would be identified by some
 * naming scheme, and then multiple file formats could be used. This
 * would also make it easy to support different underlying
 * representation of the levels (as opposed to the exclusive use of
 * <code>SimpleBoard</code>).
 */
public class DungeonFactory {

    protected static final Logger logger = 
	Logger.getLogger(DungeonFactory.class.getName());

    /**
     * This method takes a <code>StreamTokenizer</code> that is setup at
     * the start of a single dungeon file, and loads all the data, creating
     * the AIs, stitching together all connectors between levels, etc. At
     * the end a single <code>Connector</code> is provded as an entry
     * point to the dungeon.
     *
     * @param stok the stream to tokenize
     * @param gameName the name of the dungeon this is being loaded into
     * @param lobby the lobby
     *
     * @return a <code>GameConnector</code> that is the
     *         connection between the dungeon and the lobby
     *
     * @throws IOException if the stream isn't formatted correctly
     */
    public static GameConnector loadDungeon(StreamTokenizer stok, 
					    String gameName,
					    Game lobby) throws IOException {
	
        DataManager dataManager = AppContext.getDataManager();

        // the prefix for all level names
        String levelPrefix = gameName + ":" + Level.NAME_PREFIX;

        // details about where we enter the dungeon
        String entryLevel = null;
        int entryX = 0;
        int entryY = 0;

        // the collection of boards and levels
        HashMap<String,SimpleBoard> boards = new HashMap<String,SimpleBoard>();
        HashMap<String,ManagedReference<SimpleLevel>> levelRefs =  //simplelevel
            new HashMap<String,ManagedReference<SimpleLevel>>();

        // the various kinds of connectors
        HashSet<ConnectionData> connections = new HashSet<ConnectionData>();
        HashSet<ConnectionData> oneWays = new HashSet<ConnectionData>();
        HashSet<ConnectionData> playerConnectors =
            new HashSet<ConnectionData>();

        // the collection of Monster (AI) and NPC characters
        // to AICharacterManager
        HashMap<String,HashSet<ManagedReference<AICharacterManager>>> npcMap =
            new HashMap<String,HashSet<ManagedReference<AICharacterManager>>>();
        HashMap<String,HashSet<ManagedReference<AICharacterManager>>> aiMap =
            new HashMap<
		String, HashSet<ManagedReference<AICharacterManager>>>();

        // first, parse the data file itself
        while (stok.nextToken() != StreamTokenizer.TT_EOF) {
            if (stok.sval.equals("EntryPoint")) {
                // an Entry is LEVEL X_POS Y_POS
                stok.nextToken();
                entryLevel = levelPrefix + stok.sval;
                stok.nextToken();
                entryX = (int)(stok.nval);
                stok.nextToken();
                entryY = (int)(stok.nval);
            } else if (stok.sval.equals("DefineLevel")) {
                // levels are handled separately by SimpleLevel
                stok.nextToken();
                String levelName = levelPrefix + stok.sval;
		if (boards.containsKey(levelName)) {
		    logger.warning("duplicate name defined for level: " + 
				   levelName);		    
		}
                boards.put(levelName, SimpleBoard.parse(stok));

            } else if (stok.sval.equals("Connection")) {
                connections.add(readConnection(stok, levelPrefix));
            } else if (stok.sval.equals("OneWayConnection")) {
                oneWays.add(readConnection(stok, levelPrefix));
            } else if (stok.sval.equals("PlayerConnection")) {
                playerConnectors.add(readConnection(stok, levelPrefix));
            } else if (stok.sval.equals("NPC")) {
                // an NPC is LEVEL NAME ID MESSAGE_1 [ ... MESSAGE_N ]
                stok.nextToken();
                String levelName = levelPrefix + stok.sval;
                stok.nextToken();
                String npcName = stok.sval;
                stok.nextToken();
                int id = (int)(stok.nval);
                stok.nextToken();
                int count = (int)(stok.nval);
                String [] messages = new String[count];
                for (int i = 0; i < count; i++) {
                    stok.nextToken();
                    messages[i] = stok.sval;
                }

                // create the manager for the NPC and the NPC itself
                AICharacterManager aiCMR =
                    AICharacterManager.newInstance();
                NPCharacter npc =
                    new NPCharacter(id, npcName, messages, aiCMR);
                aiCMR.setCharacter(npc);

                // put it into a bucket for the given level, creating the
                // bucket if it doesn't already exist
                // to AICharacterManager
                HashSet<ManagedReference<AICharacterManager>> set =
		    npcMap.get(levelName);
                if (set == null) {
                    set = new HashSet<ManagedReference<AICharacterManager>>();
                    npcMap.put(levelName, set);
                }
                set.add(dataManager.createReference(aiCMR));
            } else if (stok.sval.equals("Monster")) {
                // a Monster is LEVEL TYPE ID
                stok.nextToken();
                String levelName = levelPrefix + stok.sval;
                stok.nextToken();
                String type = stok.sval;
                stok.nextToken();
                int id = (int)(stok.nval);

                // create the manager and get the right instance
                AICharacterManager aiCMR =
                    MonsterFactory.getMonster(id, type);

                // put the monster into a bucket for the given level, creating
                // the bucket if it doesn't already exist
                HashSet<ManagedReference<AICharacterManager>> set =
		    aiMap.get(levelName);
                if (set == null) {
                    set = new HashSet<ManagedReference<AICharacterManager>>();
                    aiMap.put(levelName, set);
                }
                set.add(dataManager.createReference(aiCMR));
            } else {
                throw new IOException("Unknown type: " + stok.sval +
                                      " on line " + stok.lineno());
            }
        }

        // next, create a ManagedObject for each of the levels
        for (String levelName : boards.keySet()) {
            SimpleLevel level = new SimpleLevel(levelName, gameName);
            String gloName = Game.NAME_PREFIX + levelName;
            dataManager.setBinding(gloName, level);
            levelRefs.put(levelName, dataManager.createReference(level));
        }

        // with the levels in place, we can generate the connectors and
        // assign them to their board spaces
        for (ConnectionData data : connections) {
            // create a connector and register it
            SimpleConnector connector =
                new SimpleConnector(levelRefs.get(data.level1).get(),
                                    data.level1X, data.level1Y,
                                    levelRefs.get(data.level2).get(),
                                    data.level2X, data.level2Y);

            // notify both boards of the connector
            boards.get(data.level1).setAsConnector(data.level1X, data.level1Y,
                                                   connector);
            boards.get(data.level2).setAsConnector(data.level2X, data.level2Y,
                                                   connector);
        }

        // we also get the player connectors
        for (ConnectionData data : playerConnectors) {
            // create a connector and register it
            PlayerConnector connector =
                new PlayerConnector(levelRefs.get(data.level1).get(),
                                    data.level1X, data.level1Y,
                                    levelRefs.get(data.level2).get(),
                                    data.level2X, data.level2Y);

            // notify both boards of the connector
            boards.get(data.level1).setAsConnector(data.level1X, data.level1Y,
                                                   connector);
            boards.get(data.level2).setAsConnector(data.level2X, data.level2Y,
                                                   connector);
        }

        // same for the one-ways, except that we only set one side
        for (ConnectionData data : oneWays) {
            // create the connector and register it
            OneWayConnector connector =
                new OneWayConnector(levelRefs.get(data.level2).get(),
                                    data.level2X, data.level2Y);

            // notify the source board of the connector
            boards.get(data.level1).setAsConnector(data.level1X, data.level1Y,
                                                   connector);
        }

        // also generate the entry connector, register it, and set it for
        // the entry board
        GameConnector gameConnector =
            new GameConnector(lobby, levelRefs.get(entryLevel).get(),
			      entryX, entryY);
        boards.get(entryLevel).setAsConnector(entryX, entryY, gameConnector);

        // with all the connectors in place, notify the levels
        for (ManagedReference<SimpleLevel> levelRef : levelRefs.values()) {
            SimpleLevel level = levelRef.get();
            level.setBoard(boards.get(level.getName()));
        }

        TaskManager taskManager = AppContext.getTaskManager();

        // now that the levels are all set, add the NPC characters to the
        // levels and the timer
        for (String levelName : npcMap.keySet()) {
            Level level = levelRefs.get(levelName).get();
            for (ManagedReference<AICharacterManager> mgrRef :
		     npcMap.get(levelName))
	    {
                AICharacterManager mgr = mgrRef.get();
                taskManager.schedulePeriodicTask(mgr, 0, 1700);
                //eventAg.addCharacterMgr(mgr);
                level.addCharacter(mgr);
            }
        }

        // add the Monsters too
        for (String levelName : aiMap.keySet()) {
            Level level = levelRefs.get(levelName).get();
            for (ManagedReference<AICharacterManager> mgrRef :
		     aiMap.get(levelName)) {
                AICharacterManager mgr = mgrRef.get();
                taskManager.schedulePeriodicTask(mgr, 0, 1100);
                //eventAg.addCharacterMgr(mgr);
                level.addCharacter(mgr);
            }
        }
        
        // finally add, the items
        
	// TODO: support items in file format; currently there are no
	//       items.

        // return the game connector, which is all the Dungeon needs to
        // interact with everything we've setup here
        return gameConnector;
    }
    
    /**
     * Private helper method that reads the data for one Connector.
     */
    private static ConnectionData readConnection(StreamTokenizer stok,
                                                 String namePrefix)
        throws IOException
    {
        ConnectionData data = new ConnectionData();

        stok.nextToken(); data.level1 = namePrefix + stok.sval;
        stok.nextToken(); data.level1X = (int)(stok.nval);
        stok.nextToken(); data.level1Y = (int)(stok.nval);

        stok.nextToken(); data.level2 = namePrefix + stok.sval;
        stok.nextToken(); data.level2X = (int)(stok.nval);
        stok.nextToken(); data.level2Y = (int)(stok.nval);

        logger.finer("read connection: " + data.level1 + "@" +
                           data.level1X + "," + data.level1Y);
        logger.finer("it connects to: " + data.level2 + "@" +
                           data.level2X + "," + data.level2Y);

        return data;
    }

    /**
     * Inner utility class that keeps the two points associated with a
     * Connector.
     */
    static class ConnectionData {
        public String level1, level2;
        public int level1X, level1Y, level2X, level2Y;
    }

}
