package model;

import java.util.ArrayList;
import java.util.List;

public class Memory {

    private static final Memory singletonInstance = new Memory();
    private final List<ObserverMemory> observers = new ArrayList<>();
    private CommandType lastCommandType = null;
    private boolean toReplace = false;
    private boolean continuingMathCalculation = false;
    private int countNumberTyped = 0;
    private String actualText = "";
    private String firstBufferedText = "";
    private String secondBufferedText = "";

    private Memory(){ }

    private enum CommandType { RESET, NUMBER, DIVISION, MULTIPLICATION, SUM, SUBTRACTION, EQUAL, COMMA; };

    public static Memory getSingletonInstance(){
        return singletonInstance;
    }

    public List<ObserverMemory> getObservers() { return observers; }

    public void addObserver(final ObserverMemory observer) {
        getObservers().add(observer);
    }

    public CommandType getLastCommandType() { return lastCommandType; }

    public void setLastCommandType(final CommandType lastCommandType) { this.lastCommandType = lastCommandType; }

    public boolean isToReplace() { return toReplace; }

    public void setToReplace(final boolean toReplace) { this.toReplace = toReplace; }

    public boolean isContinuingMathCalculation() { return continuingMathCalculation; }

    public void setContinuingMathCalculation(final boolean continuingMathCalculation) { this.continuingMathCalculation = continuingMathCalculation; }

    public int getCountNumberTyped() { return countNumberTyped; }

    public void setCountNumberTyped(final int countNumberTyped) {
        if(countNumberTyped == 0) {
            this.countNumberTyped = countNumberTyped;
        } else {
            this.countNumberTyped += countNumberTyped;
        }
    }

    public void setActualText(final String actualText) { this.actualText = actualText; }

    public String getActualText() { return actualText.isEmpty() ? "0" : actualText; }

    public String getFirstBufferedText() { return firstBufferedText; }

    public void setFirstBufferedText(final String firstBufferedText) { this.firstBufferedText = firstBufferedText; }

    public String getSecondBufferedText() { return secondBufferedText; }

    public void setSecondBufferedText(final String secondBufferedText) { this.secondBufferedText = secondBufferedText; }

    public void processCommand(final String typedValue) {
        CommandType commandType = detectCommandType(typedValue);
        if(commandType == null){
            return;
        } else {
            switch(commandType){
                case RESET:
                    resetMemory();
                    break;
                case NUMBER:
                    setCountNumberTyped(1);
                    storeNumberTyped(typedValue);
                    break;
                case COMMA:
                    setCountNumberTyped(0);
                    addComma(typedValue);
                    break;
                case SUM:
                case SUBTRACTION:
                case MULTIPLICATION:
                case DIVISION:
                    setCountNumberTyped(0);
                    setLastCommandType(commandType);
                    setActualText("");
                    break;
                case EQUAL:
                    calculateMathOperation();
                    break;
            }
        }
        getObservers().forEach(observer -> observer.changeValue(getActualText()));
    }

    private CommandType detectCommandType(final String valueToBeVerified){
        try{
            Integer.parseInt(valueToBeVerified);
            return CommandType.NUMBER;
        } catch (NumberFormatException numberFormatException){
            if(valueToBeVerified.equals("AC")){
                return CommandType.RESET;
            } else if (valueToBeVerified.equals("/")){
                return CommandType.DIVISION;
            } else if (valueToBeVerified.equals("X")){
                return CommandType.MULTIPLICATION;
            } else if (valueToBeVerified.equals("+")){
                return CommandType.SUM;
            } else if (valueToBeVerified.equals("-")){
                return CommandType.SUBTRACTION;
            } else if (valueToBeVerified.equals("=")){
                return CommandType.EQUAL;
            } else if ((valueToBeVerified.equals(",")) && (getActualText().contains(",") == false)){
                return CommandType.COMMA;
            }
        }
        return null;
    }

    private void resetMemory() {
        setActualText("");
        setFirstBufferedText("");
        setSecondBufferedText("");
        setToReplace(false);
        setContinuingMathCalculation(false);
        setCountNumberTyped(0);
        setLastCommandType(null);
    }

    private void addComma(final String typedValue) {
        setToReplace((getActualText().contains(",") == false) ? true : false);
        setActualText(isToReplace() ? getActualText().concat(typedValue) : "");
    }

    private void storeNumberTyped(final String typedValue) {
        // TODO: Adicionar tratamento de numeros negativos
        if((getCountNumberTyped() > 1) || (getActualText().contains(","))) {
            setActualText(getActualText().concat(typedValue));
        } else {
            setActualText(typedValue);
        }

        if ((getLastCommandType() == null) && (isContinuingMathCalculation() == false)) {
            setFirstBufferedText(getActualText());
            return;
        } else {
            setSecondBufferedText(getActualText());
            return;
        }

    }

    private void calculateMathOperation(){
        double firstBufferTextCommaConversion = Double.parseDouble(getFirstBufferedText().replace(",", "."));
        double secondBufferTextCommaConversion = Double.parseDouble(getSecondBufferedText().replace(",", "."));
        double resultMathOperation = 0;

        setFirstBufferedText("");
        setSecondBufferedText("");

        if(getLastCommandType().equals(CommandType.SUM)){
            resultMathOperation = (firstBufferTextCommaConversion + secondBufferTextCommaConversion);
        } else if(getLastCommandType().equals(CommandType.SUBTRACTION)){
            resultMathOperation = (firstBufferTextCommaConversion - secondBufferTextCommaConversion);
        } else if(getLastCommandType().equals(CommandType.MULTIPLICATION)) {
            resultMathOperation = (firstBufferTextCommaConversion * secondBufferTextCommaConversion);
        } else if(getLastCommandType().equals(CommandType.DIVISION)) {
            resultMathOperation = (firstBufferTextCommaConversion / secondBufferTextCommaConversion);
        }
        String resultMathToString = Double.toString(resultMathOperation).replace(".", ",");
        resultMathToString = resultMathToString.contains(",0") ? resultMathToString.replace(",0", "") : resultMathToString;
        setActualText(resultMathToString);
        setFirstBufferedText(resultMathToString);
        setContinuingMathCalculation(true);
    }

}
