import java.util.Scanner;

public class GUI {
    public void start() {
        RTree<Integer> tree = new RTree<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            int option = nextStep();
            if (option == 0) {
                int flag = 1;
                while (flag > 0) {
                    insertInt(tree);
                    System.out.println("\nДобавить элемент ещё один элемент?");
                    flag = scanner.nextInt();
                }
            }
            else if (option == 1) {
                System.out.println("Введите x: ");
                float x = scanner.nextFloat();
                System.out.println("Введите y: ");
                float y = scanner.nextFloat();
                float[] coords = new float[]{x, y};
                System.out.println("x + ");
                float xPlus = scanner.nextFloat();
                System.out.println("y + ");
                float yPlus = scanner.nextFloat();
                float[] dimensions = new float[]{xPlus, yPlus};
                System.out.println("Введите значение: ");
                int value = scanner.nextInt();
                tree.delete(coords, dimensions, value);
            }
            else if (option == 2) {
                System.out.println("Введите x: ");
                float x = scanner.nextFloat();
                System.out.println("Введите y: ");
                float y = scanner.nextFloat();
                float[] coords = new float[]{x, y};
                System.out.println("x + ");
                float xPlus = scanner.nextFloat();
                System.out.println("y + ");
                float yPlus = scanner.nextFloat();
                float[] dimensions = new float[]{xPlus, yPlus};
                System.out.println(tree.search(coords, dimensions));
            }
            else if (option == 3) {
                tree.clear();
            }
            else if (option == 4) {
                return;
            }
        }
    }

    private void insertInt(RTree<Integer> tree) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите x: ");
        float x = scanner.nextFloat();
        System.out.println("Введите y: ");
        float y = scanner.nextFloat();
        float[] coords = new float[]{x, y};
        System.out.println("x + ");
        float xPlus = scanner.nextFloat();
        System.out.println("y + ");
        float yPlus = scanner.nextFloat();
        float[] dimensions = new float[]{xPlus, yPlus};
        System.out.println("Введите значение: ");
        int value = scanner.nextInt();
        tree.insert(coords, dimensions, value);
    }

    private int nextStep() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nВыберите действие:\n 0 -- Добавить элемент \n 1 -- Удалить элемент\n 2 -- Найти значения\n 3 -- Очистить дерево\n 4 -- Закончить выполнение");
        return scanner.nextInt();
    }
}