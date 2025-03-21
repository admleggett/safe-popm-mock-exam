package co.uk.jagemtech.safepopmexam.repository;

import co.uk.jagemtech.safepopmexam.model.Choice;
import co.uk.jagemtech.safepopmexam.model.Question;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class MockQuestionRepository implements QuestionRepository {

    @Override
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        
        // Question 1
        questions.add(new Question(
            "What is SAFe's primary approach to Lean-Agile adoption?",
            Arrays.asList(
                new Choice("Bottom-up implementation across teams", false),
                new Choice("Top-down implementation starting with leadership training", true),
                new Choice("Middle-out implementation focusing on program managers", false),
                new Choice("Implementation through external consultants only", false)
            ),
            "SAFe advocates a top-down implementation approach, starting with training leaders, as this accelerates organizational change."
        ));
        
        // Question 2
        questions.add(new Question(
            "What are the four Core Values of SAFe?",
            Arrays.asList(
                new Choice("Transparency, Inspection, Adaptation, Relentless Improvement", false),
                new Choice("Alignment, Built-in Quality, Transparency, Program Execution", true),
                new Choice("Respect for People, Flow, Innovation, Relentless Improvement", false),
                new Choice("Trust, Value, Efficiency, Delivery", false)
            ),
            "The four Core Values of SAFe are Alignment, Built-in Quality, Transparency, and Program Execution."
        ));
        
        // Question 3
        questions.add(new Question(
            "In SAFe, what is the primary purpose of PI Planning?",
            Arrays.asList(
                new Choice("To create a detailed backlog for the next 6-12 months", false),
                new Choice("To align teams to a common mission and vision for the next Program Increment", true),
                new Choice("To evaluate the performance of individual team members", false),
                new Choice("To create a project budget for the fiscal year", false)
            ),
            "PI Planning aligns teams to a common mission and creates the PI plan with objectives for the upcoming Program Increment."
        ));
        
        // Question 4
        questions.add(new Question(
            "What is a key responsibility of the Product Owner in SAFe?",
            Arrays.asList(
                new Choice("Writing detailed technical specifications", false),
                new Choice("Managing team dynamics and resolving conflicts", false),
                new Choice("Defining and prioritizing the team backlog", true),
                new Choice("Conducting performance reviews of team members", false)
            ),
            "The Product Owner is responsible for defining Stories and prioritizing the Team Backlog to streamline the execution of Program priorities."
        ));
        
        // Question 5
        questions.add(new Question(
            "What is the recommended number of ARTs in a Value Stream?",
            Arrays.asList(
                new Choice("1-5", true),
                new Choice("6-10", false),
                new Choice("11-15", false),
                new Choice("There is no recommended number", false)
            ),
            "SAFe recommends 1-5 ARTs per Value Stream, with 50-125 people per ART."
        ));
        
        return questions;
    }
}
