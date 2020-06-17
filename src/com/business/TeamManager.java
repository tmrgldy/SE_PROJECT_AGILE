package com.business;

import com.persistent.Team;
import com.persistent.User;
import com.persistent.WorkItem;
import com.presentation.LoginView;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TeamManager {

    private static TeamManager TeamManagerInstance; //Singleton instance

    private static String fileAddress = "src/com/data/teamsFile.ser";
    // public File teamsFile;
    public HashMap<String, Team> teams;


    public static TeamManager getInstance() {
        if (TeamManagerInstance == null)
            TeamManagerInstance = new TeamManager();

        return TeamManagerInstance;
    }

    private TeamManager() {

        this.teams = new HashMap<String, Team>();
       /* HashMap<String, Team> map = new HashMap<>();
        //teamsFile = new File(fileAddress);

        File teamsFile = new File(fileAddress);
        if (teamsFile.length() != 0) {
            // load users file to HashMap
            try {
                FileInputStream teamFileInputStream = new FileInputStream(fileAddress);
                ObjectInputStream teamObjectInputStream = new ObjectInputStream(teamFileInputStream);
                map = (HashMap) teamObjectInputStream.readObject();
                teams.putAll(map);
                teamObjectInputStream.close();
                teamFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                //System.out.println("failed to load users file to HashMap\n");
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
                System.out.println("Class not found");
            }
        }*/


        loadTeamsFileToHashMap();

        if (isTeamExist("default"))//Default team doesn't exist - no other team is exist! Need to add also Algo&SW
        {
            addTeam("default");
            addTeam("Algo");
            addTeam("SW");
        }
    }


    /**
     * The method read teamFile file.
     */

    public void addTeam(String teamName) {
        if (!isTeamExist(teamName))
            teams.put(teamName, new Team(teamName));

    }

    public void removeTeam(String teamsName) {

        if (isTeamExist(teamsName)&& teams.get(teamsName).getUsers().size()==0 &&!teamsName.equals("default"))
            this.teams.remove(teamsName);
    }

    public Team getTeam(String teamName) {
        return this.teams.get(teamName);
    }

    public void addMemberToTeam(String username, Team team) {

        if(!username.isEmpty())
            team.addUser(username);
    }

    public void removeMemberFromTeam(String username, Team team) {
        if (isUserBelongToTeam(team.getTeamsName(), username))
            team.removeUser(username);
    }

    public Boolean isTeamExist(String teamName) {
        Team tmp = teams.get(teamName);
        return tmp != null;
    }

    public Boolean isUserBelongToTeam(String teamName, String username) {
        if (!isTeamExist(teamName))
            return false;
        else {
            for (int i = 0; i < teams.get(teamName).getUsers().size(); i++) {
                if (teams.get(teamName).getUsers().get(i).equals(username))
                    return true;

            }
            return false;
        }
    }

    public Boolean updateTeamsFile() {
        FileOutputStream fos =null;
        try {
            fos = new FileOutputStream(fileAddress);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(teams);
            oos.close();
            fos.close();
            System.out.printf("Serialized HashMap data is saved in teamsFile.ser");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return(fos!=null);
    }


    public void updateTeamsName(String oldName, String newName) {
        Team team = teams.get(oldName);
        team.setTeamsName(newName);
        teams.remove(oldName);
        teams.put(newName, team);

        // Change team name for all users belongs to that team
        for (Map.Entry<String, User> stringUserEntry : LoginView.userManager.users.entrySet()) {
            String username = stringUserEntry.getKey();
            String userTeam = stringUserEntry.getValue().getTeamName();
            if (userTeam.equals(oldName))
                LoginView.userManager.updateUserTeam(username, newName);
        }

        //Change team name for all work items associated with that team
        for (Map.Entry<Integer, WorkItem> workItemEntry : WorkItemManager.getInstance().workItems.entrySet()) {
            Integer id = workItemEntry.getKey();
            String teamName = workItemEntry.getValue().getTeam();
            if (teamName != null && teamName.equals(oldName))
                workItemEntry.getValue().setTeam(newName);
        }

    }

    public void setFileAddress(String newAddress)
    {
        fileAddress = newAddress;
    }

    public void loadTeamsFileToHashMap()
    {
        HashMap<String, Team> map = new HashMap<>();

        File teamsFile = new File(fileAddress);
        if (teamsFile.length() != 0) {
            // load users file to HashMap
            try {
                FileInputStream teamFileInputStream = new FileInputStream(fileAddress);
                ObjectInputStream teamObjectInputStream = new ObjectInputStream(teamFileInputStream);
                map = (HashMap) teamObjectInputStream.readObject();
                teams.putAll(map);
                teamObjectInputStream.close();
                teamFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                //System.out.println("failed to load users file to HashMap\n");
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
                System.out.println("Class not found");
            }
        }

        if (teams.get("default") == null)//Default team
            addTeam("default");
        addTeam("Algo");
        addTeam("SW");
        //teams.put("default", new Team("default"));
    }
}


 /*try
            {

                FileInputStream fis = new FileInputStream(fileAddress);
                ObjectInputStream ois = new ObjectInputStream(fis);
                map = (HashMap) ois.readObject();
                teams.putAll(map);
                ois.close();
                fis.close();
            }catch(IOException ioe)
            {
                ioe.printStackTrace();
                return;
            }catch(ClassNotFoundException c)
            {
                System.out.println("Class not found");
                c.printStackTrace();
                return;
            }
            System.out.println("Deserialized HashMap..");
            // Display content using Iterator
            Set set = map.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                System.out.print("key: "+ mentry.getKey() + " & Value: ");
                System.out.println(mentry.getValue());
            }


            if(teams.get("default")==null)//Default team
                teams.put("default", new Team("default"));


        }*/