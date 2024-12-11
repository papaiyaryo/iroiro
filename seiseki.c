#include <stdio.h>


int main() {
    struct seiseki {
        char name[20];
        int kokugo;
        int sansu;
        int sum;
    };
    struct seiseki s[41];
    int i = 0;
    while (1) {
        scanf("%s %d %d", s[i].name, &s[i].kokugo, &s[i].sansu);
        if (s[i].kokugo <0) break;

        s[i].sum = s[i].kokugo + s[i].sansu ;
        printf("%s %d\n", s[i].name, s[i].sum);
        i++;
    }

    return 0;
}
