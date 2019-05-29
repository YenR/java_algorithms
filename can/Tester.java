/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ads1.ss14.can;

public class Tester {
    public static void main(String[] args) {
        String[] arg = new String[5];
        arg[0] = "public_instances/0011";
        arg[1] = "-d";//"-d";
        arg[2] = "-r";//"-t";
        arg[3] = "-t";//"-c";
        arg[4] = "-s";//"-s";
        Main.main(arg);
    }
}
