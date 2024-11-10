// for dev only

const theResp={
    "nom": "Lille",
    "insee":"59350",
    "annee": "2019",
    "stats": [
        {"surface": ">0 <=1", "habitants": 32543, "annee": "2019", "barColor": "#7b7b7b"},
        {"surface": ">1 <=2", "habitants": 37190, "annee": "2019", "barColor": "#858585"},
        {"surface": ">2 <=3", "habitants": 28575, "annee": "2019", "barColor": "#8f8f8f"},
        {"surface": ">3 <=4", "habitants": 17950, "annee": "2019", "barColor": "#999999"},
        {"surface": ">4 <=5", "habitants": 13575, "annee": "2019", "barColor": "#a3a3a3"},
        {"surface": ">5 <=6", "habitants": 6975, "annee": "2019", "barColor": "#adadad"},
        {"surface": ">6 <=7", "habitants": 11010, "annee": "2019", "barColor": "#b7b7b7"},
        {"surface": ">7 <=8", "habitants": 4500, "annee": "2019", "barColor": "#c1c1c1"},
        {"surface": ">8 <=9", "habitants": 3956, "annee": "2019", "barColor": "#cbcbcb"},
        {"surface": ">9 <=10", "habitants": 3499, "annee": "2019", "barColor": "#d5d5d5"},
        {"surface": ">10 <=11", "habitants": 2642, "annee": "2019", "barColor": "#9ee88f"},
        {"surface": ">11 <=12", "habitants": 2262, "annee": "2019", "barColor": "#9ee88f"},
        {"surface": ">12 <=15", "habitants": 7668, "annee": "2019", "barColor": "#1a9900"},
        {"surface": ">15 <=20", "habitants": 8461, "annee": "2019", "barColor": "#1a9900"},
        {"surface": ">20 <=25", "habitants": 5989, "annee": "2019", "barColor": "#1a9900"},
        {"surface": ">25 <=45", "habitants": 13443, "annee": "2019", "barColor": "#1a9900"},
        {"surface": ">45 <=200", "habitants": 11123, "annee": "2019", "barColor": "#1a9900"}
    ],
    "seuils": [
        {"surface": "<10>", "habitants": 158982, "ratio":"75,5 %", "annee": "2019", "barColor": "#7b7b7b"},
        {"surface": ">=10 <12", "habitants": 4904, "ratio":"2,3 %", "annee": "2019", "barColor": "#9ee88f"},
        {"surface": ">=12", "habitants": 46682, "ratio":"22,2 %", "annee": "2019", "barColor": "#1a9900"},
    ]
};


/*
console.log(theResp["stats"]);
const labels = [...new Set(theResp["stats"].map(value => value.surface))];
const habitants = [...new Set(theResp["stats"].map(value => value.habitants))];
*/
/*
const orders = labels.map(value => {
    return array.reduce((previousValue, currentValue) =>  {
        console.log(currentValue);
        if (currentValue.product_name === value) {
            return previousValue + 1;
        }
        else {
            return  previousValue;
        }
    }, 0);
});
*/
