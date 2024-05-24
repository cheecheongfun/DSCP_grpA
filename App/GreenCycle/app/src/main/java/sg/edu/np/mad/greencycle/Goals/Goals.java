package sg.edu.np.mad.greencycle.Goals;


import java.util.Date;

public class Goals {
    private int goalid, goals_number,goals_completion;
    private Date created_date,end_date;

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

    public int getGoals_completion() {
        return goals_completion;
    }

    public void setGoals_completion(int goals_completion) {
        this.goals_completion = goals_completion;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public Goals(int goalid, int goals_number, int goals_completion, Date created_date, Date end_date) {
        this.goalid = goalid;
        this.goals_number = goals_number;
        this.goals_completion = goals_completion;
        this.created_date = created_date;
        this.end_date = end_date;
    }
}
