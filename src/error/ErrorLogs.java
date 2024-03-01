package error;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ErrorLogs {
    public HashMap<Integer, Error> errorList = new HashMap<>();
    private FileOutputStream outFile;

    public ErrorLogs(String outfilePath) throws FileNotFoundException {
        this.outFile = new FileOutputStream(outfilePath);
    }

    public void addErrorMsg(Error error) {
        errorList.put(error.getLine(), error);
    }

    public void printErrorLogs() throws IOException {
        ArrayList<Error> errors = new ArrayList<>(errorList.values());
        errors.sort(Error::compareTo);
        for (Error error : errors) {
            String string = error.toString();
            outFile.write(string.getBytes());
            System.out.print(string);
        }
    }

    public boolean hasErrorLogs(){
        return this.errorList.size() > 0;
    }

}
