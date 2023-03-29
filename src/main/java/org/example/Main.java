package org.example;

import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        String path = "src/main/resources/withoutchoicestate.json";
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
            //check if the basic capability map syntax is valid or not
            if(validationCapabilityMapSyntax == true) {
                JSONObject states = (JSONObject) jsonObject.get("States");
                String startAt = (String) jsonObject.get("StartAt");
                String jsonString = new String(Files.readAllBytes(Paths.get(path)));
                // check if there are duplicate states in the object field "States"
                checkDuplicateStatesResult = checkDuplicateStates(states,jsonString);
                if(checkDuplicateStatesResult == true)
                {
                    //validate the transition from start state till the end
                    validateTransitionFromStatesResult = validateTransitionFromStates(states, startAt);
                }
            }
            else
                System.out.println("Invalid Capability Map Syntax");
            if(checkDuplicateStatesResult == true && validateTransitionFromStatesResult == true && validationCapabilityMapSyntax == true)
            {
                System.out.println("Valid Capability Map");
            }
            else
            {
                System.out.println("Invalid Capability Map");
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
    static boolean checkDuplicateStates(JSONObject states, String jsonString)
    {
        boolean checkDuplicateStatesResult = true;
        Set<String> keys = states.keySet();
        String keysStringArray[] = keys.toArray(new String[keys.size()]);
        //System.out.println("Keys: "+keys);
        //System.out.println("Length of set of keys in object: "+keys.size());
        String key = "";
        int startIndex,firstIndex;
        int counter;
        //System.out.println(jsonString);
        for(int i=0;i<keysStringArray.length;i++)
        {
            key = keysStringArray[i];
            startIndex = 0;
            firstIndex = 0;
            counter = 0;
            //System.out.println("Key: "+key);
            while(startIndex >= 0 && startIndex <= jsonString.length()) {
                firstIndex = jsonString.indexOf(key,startIndex);
                //System.out.println("First Index of word: "+firstIndex);
                if(firstIndex == -1)
                    break;
                startIndex = firstIndex + key.length();
                //System.out.println("New start index: "+startIndex);
                ++counter;
            }
            //System.out.println("Counter for key " + key +": "+counter);
            if(counter > 2)
            {
                System.out.println("Duplicate keys found");
                checkDuplicateStatesResult = false;
                break;
            }
        }
        System.out.println("checkDuplicateStatesResult: " + checkDuplicateStatesResult);
        return checkDuplicateStatesResult;
    }

    //validate the fields of each state
    static boolean validateCapabilityMapStates(JSONObject currentState, String type)
    {
        boolean validateCapabilityMapStatesResult = false;
        //System.out.println("validateCapabilityMapStates Test");
        if(type.equals("Task") == true)
        {
            validateCapabilityMapStatesResult = validateTaskState(currentState);
        }
        if(type.equals("Choice") == true)
        {
            validateCapabilityMapStatesResult = validateChoiceState(currentState);
        }
        return validateCapabilityMapStatesResult;
    }

    //take the firstState and transition till the end
    static boolean validateTransitionFromStates(JSONObject states, String startAt)
    {
        boolean validateResult = false;
        boolean nextStateValid = true;
        boolean typeValidateResult = false;
        boolean validateCapabilityMapStatesResult = false;
        String type = "";
        boolean end = false;
        //Succeed state and Fail state (Terminal State)
        //boolean succeed = false;
        //boolean fail = false;
        String statePointer= startAt;
        JSONObject currentState = null;
        JSONObject nextState = null;
        //System.out.println("States: "+states);
        while(nextStateValid == true)
        {

            //System.out.println("State pointer: "+statePointer);
            currentState = (JSONObject) states.get(statePointer);
            //System.out.println("Current state: "+currentState);
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
                //System.out.println("typeValidateResult: "+typeValidateResult);
                //validate the structure of a particular state
                validateCapabilityMapStatesResult = validateCapabilityMapStates(currentState,type);
                //System.out.println("End: "+end);
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
                            //System.out.println("Next state pointer: "+statePointer);
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
                                    //System.out.println("Invalid Transition");
                                    //System.out.println("No " + statePointer + " found.");
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
        //System.out.println(states);
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

    //check if the
    static boolean validateChoiceState(JSONObject choiceState)
    {
        boolean validateChoiceStateResult = true;


        return validateChoiceStateResult;
    }

    //check if the Resource field is present or not (MANDATORY)
    //check if the TimeoutSeconds and HeartbeatSeconds fields are positive integers
    //check if the HeartbeatSeconds interval is smaller than the TimeoutSeconds value
    //A Task State may have "TimeoutSecondsPath" and "HeartbeatSecondsPath" fields (PENDING - VALIDATION NOT DONE)
    //A Task State MAY include a "Credentials" field, whose value MUST be a JSON object whose value is defined by the interpreter. (PENDING - VALIDATION NOT DONE)
    static boolean validateTaskState(JSONObject taskState)
    {
        boolean validateTaskStateResult = false;
        boolean validateTimeoutSeconds = true;
        boolean validateHeartbeatSeconds = true;
        System.out.println("Task State: "+taskState);
        String resource = (String) taskState.get("Resource");
        long timeoutSeconds = 0;
        long heartbeatSeconds = 0;
        try
        {
            timeoutSeconds = (long) taskState.get("TimeoutSeconds");
            heartbeatSeconds = (long) taskState.get("HeartbeatSeconds");
            if((heartbeatSeconds >= timeoutSeconds))
            {
                validateTimeoutSeconds = false;
                validateHeartbeatSeconds = false;

            }
            if(heartbeatSeconds <= 0 || timeoutSeconds <= 0)
            {
                validateTimeoutSeconds = false;
                validateHeartbeatSeconds = false;
            }
        }
        catch(NullPointerException e)
        {
            validateTimeoutSeconds = true;
            validateHeartbeatSeconds = true;
        }

        if(resource != null && validateTimeoutSeconds == true && validateHeartbeatSeconds == true)
            validateTaskStateResult = true;
        //System.out.println("validateTaskStateResult: "+validateTaskStateResult);
        return validateTaskStateResult;
    }
}