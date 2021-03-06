package com.business;

import com.persistent.User;
import com.persistent.WorkItem;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/***
 Singleton class:
 Managing users documentation, responsible for creating, editing and removing users
 ***/

public class UserManager {

    private static UserManager UserManagerInstance; //Singleton instance

    private static String userFileAddress = "src/com/data/usersFile.ser";
    public User loggedInUser;
    public HashMap<String, User> users;

    public static UserManager getInstance() {
        if (UserManagerInstance == null)
            UserManagerInstance = new UserManager();
        return  UserManagerInstance;
    }

    private UserManager() {
        try {
            users = new HashMap<>();
            loadUsersFileToHashMap();

            /* - Create admin user
               - An admin user must exist in the system to ensure
                 that there is at least one user with all permissions
               - For adding a user must be a team,
               - create default team*/
            TeamManager.getInstance().addTeam("default");
            addUser("admin","admin", User.PermissionLevel.admin,"default");

        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed to create UserManager");
        }
    }

    public boolean addUser(String username, String password, User.PermissionLevel permission, String teamName)
        //function for add user if the user not exist in system
    {
        //validation the username && create admin user or other user
        if (username != null && (username.equals("admin") || isActionPermitted())) {
            //check if user not exist in system
            if (!(isUserExist(username))) {
                //check if all inputs are valid
                if ((password == null) || !(TeamManager.getInstance().isTeamExist(teamName))) {
                    System.out.println(username+" user not created\n");
                    return false;
                }
                //create user object
                User newUser = new User(username, password, permission, teamName);
                //insert new user to team list
                TeamManager.getInstance().addMemberToTeam(username, TeamManager.getInstance().teams.get(teamName));
                //insert new user to users HashMap
                users.put(username, newUser);
                //update the user file with new user
                updateUsersFile();
                System.out.println(username+" user created\n");
                return true;
            }
        }
        System.out.println(username+" user not created\n");
        return false;
    }

    public Boolean isUserExist(String username) {
        return users.containsKey(username);
    }

    public int removeUser(String username)
        /*removeUser
          return: 1- user removed
                  2- Action no permitted
                  3- Invalid to edit admin user
                  4- user can't remove himself
         */
    {
        //check permission
        if (isActionPermitted()) {
            // user admin can't be removed
            if (!(username.equals("admin"))) {
                //user can't remove himself
                if (!(username.equals(loggedInUser.getUserName()))) {
                    //remove user from the team list
                    TeamManager.getInstance().removeMemberFromTeam(username, TeamManager.getInstance().teams.get(users.get(username).getTeamName()));
                    //change the owner of all WI under the username to Unassigned
                    for (Map.Entry<Integer, WorkItem> entry : WorkItemManager.getInstance().workItems.entrySet()) //adding rows
                        if (entry.getValue().getOwner() != null && entry.getValue().getOwner().equals(users.get(username).getUserName()))
                            entry.getValue().setOwner("Unassigned");
                    //remove user from users HashMap
                    users.remove(username, users.get(username));
                    //update file
                    updateUsersFile();
                    System.out.println(username+" user removed\n");
                    return 1;
                }
                //user can't remove himself
                else {
                    System.out.println(username+" user can't remove himself\n");
                    return 4;
                }
            }
            // user admin can't be removed
            else {
                System.out.println("Invalid to edit admin user\n");
                return 3;
            }
        }
        //check permission
        else {
            System.out.println("Action no permitted\n");
            return 2;
        }
    }

    public int updateUserPermission(String username, User.PermissionLevel newPermission)
        /*updateUserPermission
          return: 1- user updated
                  2- Action no permitted
                  3- Invalid to edit admin user
          */
    {
        if (isActionPermitted()){
            if (!(username.equals("admin"))){
                users.get(username).setPermissionLevel(newPermission);
                updateUsersFile();
                return 1;
            }
            else{
                System.out.println("Invalid to edit admin user\n");
                return 3;
            }
        }
        else System.out.println("Action no permitted\n");
        return 2;
    }


    public int updateUserTeam(String username, String newTeamName)
        /*updateUserTeam
          return: 1- user updated
                  2- Action no permitted
                  3- Invalid to edit admin user
                  4- Team not exist
         */
    {
        if (isActionPermitted()){
            if( !(username.equals("admin"))) {
                //check if new Team exist
                if (TeamManager.getInstance().teams.containsKey(newTeamName)){
                    //remove the user from previous team
                    if (TeamManager.getInstance().teams.containsKey(users.get(username).getTeamName())){
                        TeamManager.getInstance().removeMemberFromTeam(username, TeamManager.getInstance().teams.get(users.get(username).getTeamName()));
                        //set user team to new team
                        users.get(username).setTeam(newTeamName);
                        //insert the user to current list team
                        TeamManager.getInstance().addMemberToTeam(username, TeamManager.getInstance().teams.get(users.get(username).getTeamName()));
                        updateUsersFile();
                        return 1;
                    }
                    else {
                        System.out.println((users.get(username).getTeamName()) + " team not exist\n");
                        return 4;
                    }
                }
                else {
                    System.out.println("new team:"+newTeamName+" not exist\n");
                    return 4;
                }
            }
            else{
                System.out.println("Invalid to edit admin user\n");
                return 3;
            }
        }
        else System.out.println("Action no permitted\n");
        return 2;
    }

    public Integer login(String username,String password)
        /*login
          return: 0- User not exist
                  1- login success
                  2- invalid password
         */
    {
        if (isUserExist(username))
            if ((users.get(username).getPassword().equals(password))) {
                this.loggedInUser = users.get(username);
                return 1;
            }
            else{
                  System.out.println("invalid password\n");
                  return 2;
                }
         System.out.println("User not exist\n");
         return 0;
    }

    public Boolean isActionPermitted(){
        return (this.loggedInUser.getPermissionLevel()==User.PermissionLevel.manager || this.loggedInUser.getPermissionLevel()==User.PermissionLevel.admin);
    }

    public void loadUsersFileToHashMap() {
        HashMap<String, User> map = new HashMap<>();
        File usersFile = new File(userFileAddress);

        //if the file not empty,need to load users file to HashMap
        if (usersFile.length() != 0) {
            // load users file to HashMap
            try {
                FileInputStream userFileInputStream = new FileInputStream(userFileAddress);
                ObjectInputStream userObjectInputStream = new ObjectInputStream(userFileInputStream);
                map = (HashMap) userObjectInputStream.readObject();
                users.putAll(map);
                userObjectInputStream.close();
                userFileInputStream.close();
            } catch (IOException | ClassNotFoundException e) {
                return;
            }
        }
    }

    public void updateUsersFile(){
        try {
            FileOutputStream userFileOutputStream = new FileOutputStream(userFileAddress);
            ObjectOutputStream userObjectOutputStream = new ObjectOutputStream(userFileOutputStream);
            userObjectOutputStream.writeObject(users);
            userObjectOutputStream.close();
            userFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failed to update user file from HashMap\n");
        }
    }

    public static void setUserFileAddress(String fileAddress)
    // only change for test purposes
    {
        UserManager.userFileAddress = fileAddress;
    }
}