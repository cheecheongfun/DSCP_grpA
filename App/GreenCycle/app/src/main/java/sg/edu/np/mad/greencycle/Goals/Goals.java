package sg.edu.np.mad.greencycle.Goals;
//Lee Jun Rong S10242663

public class Goals {
    private int goalid, goals_number;

    private String goal_name,goals_completion,created_date,end_date;


    public int getGoalid() {
        return goalid;
    }

    public void setGoalid(int goalid) {
        this.goalid = goalid;
    }

    public int getGoals_number() {
        return goals_number;
    }

    public void setGoals_number(int goals_number) {
        this.goals_number = goals_number;
    }

    public String getGoals_completion() {
        return goals_completion;
    }

    public void setGoals_completion(String goals_completion) {
        this.goals_completion = goals_completion;
    }

    public String getGoal_name() {
        return goal_name;
    }

    public void setGoal_name(String goal_name) {
        this.goal_name = goal_name;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public Goals(int goalid, int goals_number, String goals_completion, String goal_name, String created_date, String end_date) {
        this.goalid = goalid;
        this.goals_number = goals_number;
        this.goals_completion = goals_completion;
        this.goal_name = goal_name;
        this.created_date = created_date;
        this.end_date = end_date;
    }

    // Default constructor (no-argument constructor)
    public Goals() {
        // Required by Firebase for deserialization
    }
}
