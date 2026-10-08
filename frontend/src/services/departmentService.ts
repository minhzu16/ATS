import api from "@/lib/api";
import type { Department } from "@/types/department";

export const departmentService = {
  async getAllDepartments(): Promise<Department[]> {
    const { data } = await api.get<Department[]>("/departments");
    return data;
  },
};
