package org.example;

import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.*;


public class Main {

    String filepath ="";

    public Main(String filepath)
    {
        this.filepath = filepath;
    }
    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        //can put filepath for needed json file when an instance/object of this class is made; for now we are using this json file for testing.
        try (FileReader reader = new FileReader("src/main/resources/withoutchoicestate.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject=(JSONObject)obj;
            boolean validationCapabilityMapSyntax=validateCapabilityMapSyntax(jsonObject);
            boolean checkDuplicateStatesResult = false;
            boolean validateCapabilityMapStatesResult = false;
            boolean validateTransitionFromStatesResult = false;
            if(validationCapabilityMapSyntax == true) {
                JSONObject states = (JSONObject) jsonObject.get("States");
                String startAt = (String) jsonObject.get("StartAt");
                checkDuplicateStatesResult = checkDuplicateStates(states);
                validateCapabilityMapStatesResult = validateCapabilityMapStates(states);
                validateTransitionFromStatesResult = validateTransitionFromStates(states,startAt);
            }
            else
                System.out.println("Invalid Capability Map Syntax");
            if(checkDuplicateStatesResult == true && validateCapabilityMapStatesResult == true && validateTransitionFromStatesResult == true)
            {
                System.out.println("Valid");
            }
            else
            {
                System.out.println("Not Valid");
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

    }

    //checks if the syntax of the capability map is valid or not
    //checks if the state machine has an object field "States" (Mandatory)
    //checks if the state machine has a string field named "StartAt" (Mandatory)
    //check if the "StartAt" field value matches one of the names of the "States" fields
    static boolean validateCapabilityMapSyntax(JSONObject capabilityMap)
    {
        boolean validateResult = false;
        //System.out.println("validateCapabilityMapSyntax Test");
        //System.out.println(capabilityMap);
        JSONObject states = null;
        try {
            states = (JSONObject) capabilityMap.get("States");
        }
        catch(ClassCastException e)
        {
            System.out.println("The states object field does not have any state fields.");
        }
        String startAt = (String) capabilityMap.get("StartAt");
        if(states == null)
            System.out.println("State machine does not have object field named states.");
        if(startAt == null)
            System.out.println("State machine does not have object field named StartsAt.");
        if(states != null && startAt != null )
        {
            JSONObject startAtState = (JSONObject) states.get(startAt);
            if(startAtState != null)
                validateResult = true;
        }
        return validateResult;
    }

    //check for Duplicate States in the object field named "States"
    static boolean checkDuplicateStates(JSONObject states)
    {


        return true;
    }

    //validate the fields of each state
    static boolean validateCapabilityMapStates(JSONObject states)
    {
        System.out.println("validateCapabilityMapStates Test");

        return true;
    }

    //take the firstState and transition till the end
    static boolean validateTransitionFromStates(JSONObject states, String startAt)
    {
        boolean validateResult = false;
        boolean nextStateValid = true;
        boolean typeValidateResult = false;
        String type = "";
        boolean end = false;
        String statePointer= startAt;
        JSONObject currentState = null;
        JSONObject nextState = null;
        System.out.println("States: "+states);
        while(nextStateValid == true)
        {

            System.out.println("State pointer: "+statePointer);
            currentState = (JSONObject) states.get(statePointer);
            System.out.println("Current state: "+currentState);
            type = (String) currentState.get("Type");
            try {
                end = (boolean) currentState.get("End");
            }
            catch(NullPointerException e)
            {
                end = false;
            }
            if(type != null)
            {
                typeValidateResult = validateStateType(type);
                System.out.println("typeValidateResult: "+typeValidateResult);
                System.out.println("End: "+end);
                if(typeValidateResult == true)
                {
                    if(end == true)
                    {
                        validateResult = true;
                        break;
                    }
                    else
                    {
                        if(type.equals("Choice") == false) {
                            statePointer = (String) currentState.get("Next");
                            System.out.println("Next state pointer: "+statePointer);
                            if (statePointer == null) {
                                validateResult = false;
                                break;
                            } else {
                                nextState = (JSONObject) states.get(statePointer);
                                if (nextState != null)
                                    nextStateValid = true;
                                else {
                                    nextStateValid = false;
                                    validateResult = false;
                                    System.out.println("Invalid Transition");
                                    System.out.println("No " + statePointer + " found.");
                                }
                            }
                        }
                    }
                }
                else
                {
                    validateResult = false;
                    break;
                }
            }
            else
            {
                validateResult = false;
                break;
            }

        }
        System.out.println(states);
        //System.out.println("validateTransitionFromStates Test");

        return validateResult;
    }

    //validate the input and output parameters
    static void validateParametersIO()
    {
        //System.out.println("Test");
    }

    static boolean validateStateType(String s)
    {
        boolean validateStateTypeResult =false;
        List<String> types = new ArrayList<String>();
        types.add("Pass");
        types.add("Task");
        types.add("Choice");
        types.add("Succeed");
        types.add("Fail");
        types.add("Parallel");
        types.add("Map");
        if (types.contains(s) == true)
            validateStateTypeResult = true;
        return validateStateTypeResult;
    }
}