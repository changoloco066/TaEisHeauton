package com.example.taeisheauton.parser;

import com.example.taeisheauton.model.Meditation;
import org.junit.Test;
import java.util.List;

public class MeditationParserTest {

    @Test
    public void testParse() {
        String sampleText =
                "Libro I\n" +
                        "\n" +
                        "15. - Del estoico claudia máximo\n" +
                        "Uno debe de ser dueño de si mismo, sin dejarse jamás arrastrar de las ocasiones.\n" +
                        "\n" +
                        "Libro ll\n" +
                        "\n" +
                        "10.-  Un desorden cometido por gusto era mayor delito que otro hecho con dolor\n" +
                        "\n" +
                        "14.-  Por más que tu vivieras tres mil años, y, si quieres, aún con treinta mil, con todo, haz por acordarte que ninguno pierde otra vida, al morir, que esta con la que vive, ni vive con otra qué con esta que pierde\n" +
                        "\n" +
                        "Porque ninguno puede perder, ni aquel tiempo, que ya se le pasó, ni tampoco el que aún está por venir, porque ¿Cómo se puede quitar a uno lo que uno no tiene ?\n" +
                        "\n" +
                        "Libro III\n" +
                        "\n" +
                        " 2.- Aunque ninguna hermosura ofrezcan a la vista, no obstante, por ser añadiduras que de suyo van con las demás obras de la naturaleza, a un mismo tiempo las hermosean y causan admiración.\n" +
                        "\n" +
                        "3.- No malogres el tiempo de vida que te queda en averiguar vidas ajenas, porque la curiosidad de los hechos ajenos distrae a uno del cultivo y cuidado de su mismo espíritu\n";

        MeditationParser parser = new MeditationParser();
        List<Meditation> result = parser.parse(sampleText);

        System.out.println("Total de meditaciones encontradas: " + result.size());
        System.out.println("---");
        for (Meditation m : result) {
            System.out.println(m);
        }
    }
}