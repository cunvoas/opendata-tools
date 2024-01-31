import {defineStore} from "pinia";

import {UserModel} from "@/models/UserModel";
import {AssoModel} from "@/models/AssoModel";
import {loginService} from "@/services/apis/LoginService";
import router from "@/router/index";


export const useUserStore = defineStore("userStore", { 
    //  local variables

  state: () => ({
    association:  AssoModel,
    user: (() => {
      const userString = localStorage.getItem("user");
      return userString ? JSON.parse(userString) : "";
    })(),
    returnedUrl: null as string | null,
  }),
  getters: {
    getConnectedUser(): string{
        return this.user.nom;
    }
  },
  // methods 
  actions: {
    async login(login: string, password: string): Promise<boolean> {
      try {
        const userEntity = await loginService.login(login, password);
        this.user = {
          user_id : userEntity.userId,
          token: userEntity.token,
        }
        if(userEntity.customerId.length == 0 && userEntity.captainId.length == 0){
          this.logout();
          return false;
        }
        localStorage.setItem("user", JSON.stringify(this.user));
        //if the user is not a captain or a customer, he can't access to the admin part
        
        const selectedRoleStore = useSelectedRoleStore();
        const roleStore = useRoleStore();
        //in what route the user will be redirect depending of his rights.
        if (userEntity.customerId.length >= 1) {
          this.returnedUrl = "/customer/manage-captain";
          selectedRoleStore.setCustomerIdSelected(userEntity.customerId[0]);
        } else {
          //if not a customer it's necessarily a captain
          this.returnedUrl = "/captain/manage-team";
          //set the tab to the first captain
          selectedRoleStore.setCaptainIdSelected(userEntity.captainId[0]);
        }
        //temporary we must choose the customer then.
        
        roleStore.setCaptainsId(userEntity.captainId);
        roleStore.setCustomersIdWhereIgotRights(userEntity.customerId);

        router.push({ name: 'dashboard' })
        return true;
      } catch {
        return false;
      }
    },
    logout() {
      this.user = null;
      localStorage.removeItem("user");
      router.push("/");
    },
  },
});
