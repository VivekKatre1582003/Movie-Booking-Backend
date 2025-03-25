package service;

import java.sql.SQLOutput;
import java.util.*;

public class BookMyMovieApp {

    public static void main(String[] args) {
        BookMyMovieSys mbs=new BookMyMovieSys();
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter city:");
        String city=sc.next();
        mbs.displayTheaters(city);
        mbs.displayMovies();

        System.out.println("Enter theater id and movie id:");
        int theaterId=sc.nextInt();
        int movieId=sc.nextInt();
        mbs.displayShows(movieId,theaterId);

        System.out.println("Enter show id and number of seats:");
        int showId=sc.nextInt();
        List<String> ss=Arrays.asList("A1","A2");
        mbs.bookTickets(1,showId,ss);
    }

}
